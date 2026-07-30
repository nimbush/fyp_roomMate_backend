package com.apu.roommate_filter.service;

import com.apu.roommate_filter.model.AppUser;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Weighted compatibility algorithm: Personality 40% / Lifestyle 35% / Habits 25%.
 * Returns a 0–100 score. Returns 0 immediately on gender-preference mismatch (hard exclusion).
 */
@Service
public class CompatibilityService {

    private static final double PERSONALITY_WEIGHT = 0.40;
    private static final double LIFESTYLE_WEIGHT   = 0.35;
    private static final double HABITS_WEIGHT      = 0.25;

    /** Score for a factor neither user has answered — no evidence of agreement or disagreement. */
    private static final double UNKNOWN = 0.5;

    /** Number of profile fields {@link #completeness(AppUser)} inspects. */
    private static final int SCORED_FIELD_COUNT = 8;

    public double computeScore(AppUser a, AppUser b) {
        if (!isGenderCompatible(a, b)) return 0.0;

        double personality = personalityScore(a, b);
        double lifestyle   = lifestyleScore(a, b);
        double habits      = habitsScore(a, b);

        double raw = PERSONALITY_WEIGHT * personality
                + LIFESTYLE_WEIGHT * lifestyle
                + HABITS_WEIGHT * habits;

        // Scale by how much the two profiles actually say. Without this, sparse profiles score
        // highly on unknown-vs-unknown factors alone and outrank genuinely compatible users.
        double confidence = Math.sqrt(completeness(a) * completeness(b));

        return Math.round(raw * confidence * 1000.0) / 10.0; // 0.0–100.0, one decimal place
    }

    // ── Hard exclusion ────────────────────────────────────────────────────────

    private boolean isGenderCompatible(AppUser a, AppUser b) {
        return acceptsGender(a, b.getRoommateGender())
                && acceptsGender(b, a.getRoommateGender());
    }

    private boolean acceptsGender(AppUser user, String candidateGender) {
        boolean wantsMale   = user.isPreferredMale();
        boolean wantsFemale = user.isPreferredFemale();
        if (!wantsMale && !wantsFemale) return true; // no preference – accept anyone
        if (wantsMale && wantsFemale)   return true; // accepts both
        if (candidateGender == null || candidateGender.isBlank()) return true; // unset – don't exclude
        if (wantsMale)   return "male".equalsIgnoreCase(candidateGender.trim());
        return "female".equalsIgnoreCase(candidateGender.trim());
    }

    // ── Sub-scores (each 0.0–1.0) ────────────────────────────────────────────

    private double personalityScore(AppUser a, AppUser b) {
        double hobbies = jaccardSimilarity(a.getHobbies(), b.getHobbies());
        double music   = exactMatch(a.getMusicPreference(), b.getMusicPreference());
        double pet     = exactMatch(a.getPetPreference(), b.getPetPreference());
        return hobbies * 0.50 + music * 0.25 + pet * 0.25;
    }

    private double lifestyleScore(AppUser a, AppUser b) {
        double budget    = budgetOverlapScore(a, b);
        double searching = searchingCompatibility(a, b);
        double student   = (a.isStudent() == b.isStudent()) ? 1.0 : 0.5;
        return budget * 0.50 + searching * 0.30 + student * 0.20;
    }

    private double habitsScore(AppUser a, AppUser b) {
        double sleep = exactMatch(a.getSleepingSchedule(), b.getSleepingSchedule());
        double clean = cleanlinessScore(a.getCleanliness(), b.getCleanliness());
        double smoke = (a.isSmoking() == b.isSmoking()) ? 1.0 : 0.0;
        double diet  = exactMatch(a.getDiet(), b.getDiet());
        return sleep * 0.30 + clean * 0.30 + smoke * 0.20 + diet * 0.20;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Two missing values are *unknown*, not agreement. Scoring them as a perfect match let two
     * blank profiles agree on everything and float to the top of the discovery deck.
     */
    private double exactMatch(String s1, String s2) {
        boolean aMissing = s1 == null || s1.isBlank();
        boolean bMissing = s2 == null || s2.isBlank();
        if (aMissing && bMissing) return UNKNOWN;
        if (aMissing || bMissing) return 0.0;
        return s1.trim().equalsIgnoreCase(s2.trim()) ? 1.0 : 0.0;
    }

    private double jaccardSimilarity(String a, String b) {
        Set<String> setA = tokenize(a);
        Set<String> setB = tokenize(b);
        if (setA.isEmpty() && setB.isEmpty()) return UNKNOWN;
        if (setA.isEmpty() || setB.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        return (double) intersection.size() / union.size();
    }

    private Set<String> tokenize(String csv) {
        if (csv == null || csv.isBlank()) return new HashSet<>();
        return new HashSet<>(Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .toList());
    }

    private double budgetOverlapScore(AppUser a, AppUser b) {
        double lo = Math.max(a.getMinBudget(), b.getMinBudget());
        double hi = Math.min(a.getMaxBudget(), b.getMaxBudget());
        if (lo > hi) return 0.0;
        double overlapRange = hi - lo;
        double totalRange   = Math.max(a.getMaxBudget(), b.getMaxBudget())
                            - Math.min(a.getMinBudget(), b.getMinBudget());
        return totalRange == 0 ? 1.0 : overlapRange / totalRange;
    }

    private double searchingCompatibility(AppUser a, AppUser b) {
        boolean complementary = (a.isSearchingRoommate() && b.isSearchingRoom())
                || (a.isSearchingRoom() && b.isSearchingRoommate());
        return complementary ? 1.0 : 0.5;
    }

    private double cleanlinessScore(int c1, int c2) {
        if (c1 == 0 && c2 == 0) return UNKNOWN; // both unset – no evidence either way
        if (c1 == 0 || c2 == 0) return 0.0;
        int maxDiff = 4; // scale 1–5
        return Math.max(0.0, 1.0 - (double) Math.abs(c1 - c2) / maxDiff);
    }

    // ── Profile completeness ─────────────────────────────────────────────────

    /**
     * Fraction of the fields the algorithm actually reads that the user has filled in.
     *
     * <p>{@code smoking} and {@code student} are primitive booleans and so cannot be distinguished
     * from "not answered" — they are deliberately excluded here rather than counted as answered.
     */
    private double completeness(AppUser u) {
        int filled = 0;
        if (isSet(u.getSleepingSchedule())) filled++;
        if (isSet(u.getDiet())) filled++;
        if (isSet(u.getHobbies())) filled++;
        if (isSet(u.getMusicPreference())) filled++;
        if (isSet(u.getPetPreference())) filled++;
        if (isSet(u.getRoommateGender())) filled++;
        if (u.getCleanliness() > 0) filled++;
        if (u.getMaxBudget() > 0) filled++;
        return (double) filled / SCORED_FIELD_COUNT;
    }

    private boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
