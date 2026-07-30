package com.apu.roommate_filter.service;

import com.apu.roommate_filter.model.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompatibilityServiceTest {

    private CompatibilityService service;

    @BeforeEach
    void setUp() {
        service = new CompatibilityService();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns a fully populated user with complementary searching role to `base`. */
    private AppUser baseUser() {
        AppUser u = new AppUser();
        u.setId(1L);
        u.setRoommateGender("Male");
        u.setPreferredMale(true);
        u.setPreferredFemale(true);
        u.setSleepingSchedule("Night owl");
        u.setCleanliness(3);
        u.setSmoking(false);
        u.setDiet("Vegetarian");
        u.setHobbies("Reading, Cooking, Gaming");
        u.setMusicPreference("Pop");
        u.setPetPreference("Cats");
        u.setMinBudget(500);
        u.setMaxBudget(1000);
        u.setStudent(true);
        u.setSearchingRoommate(true);
        u.setSearchingRoom(false);
        return u;
    }

    private AppUser identicalComplementaryUser() {
        AppUser u = baseUser();
        u.setId(2L);
        // Complement searching role so lifestyle sub-score = 1.0
        u.setSearchingRoommate(false);
        u.setSearchingRoom(true);
        return u;
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void identical_complementary_profiles_score_100() {
        AppUser a = baseUser();
        AppUser b = identicalComplementaryUser();
        double score = service.computeScore(a, b);
        assertThat(score).isEqualTo(100.0);
    }

    @Test
    void opposite_profiles_score_is_low() {
        AppUser a = baseUser();

        AppUser b = new AppUser();
        b.setId(3L);
        b.setRoommateGender("Female");
        b.setPreferredMale(true);
        b.setPreferredFemale(true);
        b.setSleepingSchedule("Early bird");          // different
        b.setCleanliness(1);                          // vs 3 — bigger diff
        b.setSmoking(true);                           // different
        b.setDiet("Non-vegetarian");                  // different
        b.setHobbies("Hiking, Painting, Dancing");    // no overlap with a
        b.setMusicPreference("Classical");             // different
        b.setPetPreference("Dogs");                   // different
        b.setMinBudget(1500);                         // no overlap with a (500–1000)
        b.setMaxBudget(2500);
        b.setStudent(false);                          // different
        b.setSearchingRoommate(true);                 // same as a – not complementary
        b.setSearchingRoom(false);

        double score = service.computeScore(a, b);
        assertThat(score).isLessThan(30.0);
    }

    @Test
    void gender_pref_mismatch_returns_zero() {
        // A is male and only wants a female roommate
        AppUser a = new AppUser();
        a.setId(1L);
        a.setRoommateGender("Male");
        a.setPreferredMale(false);
        a.setPreferredFemale(true);

        // B is female and only wants a female roommate — excludes A (who is male)
        AppUser b = new AppUser();
        b.setId(2L);
        b.setRoommateGender("Female");
        b.setPreferredMale(false);
        b.setPreferredFemale(true);

        double score = service.computeScore(a, b);
        assertThat(score).isZero();
    }

    @Test
    void no_budget_overlap_reduces_lifestyle_score() {
        AppUser a = baseUser();
        AppUser b = identicalComplementaryUser();
        b.setMinBudget(1500);
        b.setMaxBudget(2500); // no overlap with a's 500–1000

        double scoreNoOverlap  = service.computeScore(a, b);
        double scoreFullOverlap = service.computeScore(a, identicalComplementaryUser());

        assertThat(scoreNoOverlap).isLessThan(scoreFullOverlap);
    }

    @Test
    void partial_hobbies_overlap_gives_partial_personality_score() {
        AppUser a = baseUser();

        AppUser b = identicalComplementaryUser();
        b.setHobbies("Reading, Hiking, Cycling"); // shares only "Reading" with a

        AppUser c = identicalComplementaryUser();
        c.setHobbies("Painting, Yoga, Dancing"); // no overlap with a

        double partialScore = service.computeScore(a, b);
        double noOverlapScore = service.computeScore(a, c);

        assertThat(partialScore).isGreaterThan(noOverlapScore);
        assertThat(partialScore).isLessThan(100.0);
    }

    @Test
    void two_empty_profiles_do_not_score_highly() {
        // Regression for BE-07: blank profiles used to agree on every unanswered factor and
        // scored ~95%, outranking real profiles in the discovery deck.
        AppUser a = new AppUser();
        a.setId(10L);
        AppUser b = new AppUser();
        b.setId(11L);

        double score = service.computeScore(a, b);

        assertThat(score).isLessThan(20.0);
    }

    @Test
    void complete_profile_outranks_empty_profile() {
        AppUser me = baseUser();

        AppUser complete = identicalComplementaryUser();

        AppUser empty = new AppUser();
        empty.setId(12L);

        assertThat(service.computeScore(me, complete))
                .isGreaterThan(service.computeScore(me, empty));
    }

    @Test
    void sparser_profile_scores_lower_than_fuller_one() {
        AppUser me = baseUser();

        AppUser full = identicalComplementaryUser();

        AppUser sparse = identicalComplementaryUser();
        sparse.setId(13L);
        sparse.setHobbies(null);
        sparse.setMusicPreference(null);
        sparse.setPetPreference(null);

        assertThat(service.computeScore(me, sparse))
                .isLessThan(service.computeScore(me, full));
    }

    @Test
    void both_prefer_any_gender_never_excluded() {
        AppUser a = baseUser();
        a.setPreferredMale(false);
        a.setPreferredFemale(false); // no gender preference

        AppUser b = identicalComplementaryUser();
        b.setPreferredMale(false);
        b.setPreferredFemale(false);

        double score = service.computeScore(a, b);
        assertThat(score).isGreaterThan(0.0);
    }
}
