package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.util.SwipeDto;
import com.apu.roommate_filter.dto.util.SwipeRequest;
import com.apu.roommate_filter.mapper.SwipeMapper;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.model.Match;
import com.apu.roommate_filter.model.Swipe;
import com.apu.roommate_filter.repository.AppUserRepository;
import com.apu.roommate_filter.repository.MatchRepository;
import com.apu.roommate_filter.repository.SwipesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipesRepository swipesRepository;
    private final MatchRepository matchRepository;
    private final AppUserRepository appUserRepository;
    private final CompatibilityService compatibilityService;

    @Transactional
    public SwipeDto recordSwipe(SwipeRequest request) {
        if (request.getSwiperId() != null && request.getSwiperId().equals(request.getSwipedId())) {
            throw new IllegalArgumentException("You cannot swipe on yourself");
        }

        AppUser swiper = appUserRepository.findById(request.getSwiperId())
                .orElseThrow(() -> new RuntimeException("Swiper not found"));
        AppUser swiped = appUserRepository.findById(request.getSwipedId())
                .orElseThrow(() -> new RuntimeException("Swiped user not found"));

        if (swipesRepository.findBySwiperIdAndSwipedId(swiper.getId(), swiped.getId()).isPresent()) {
            throw new IllegalStateException("Already swiped on this user");
        }

        Swipe swipe = new Swipe();
        swipe.setSwiper(swiper);
        swipe.setSwiped(swiped);
        swipe.setLiked(request.isLiked());
        swipe.setSwipedAt(LocalDateTime.now());
        swipesRepository.save(swipe);

        if (request.isLiked()) {
            Optional<Swipe> reciprocal = swipesRepository
                    .findBySwiperIdAndSwipedId(swiped.getId(), swiper.getId());
            if (reciprocal.isPresent() && reciprocal.get().isLiked()) {
                upgradeOrCreateMatch(swiper, swiped, Match.MatchStatus.ACCEPTED);
            } else {
                createPendingMatchIfAbsent(swiper, swiped);
            }
        }

        return SwipeMapper.toDTO(swipe);
    }

    // Creates a PENDING match when the first like is recorded (user1 = initiator, user2 = responder).
    // The `/pending` query surface exposes matches where user2 = current user.
    private void createPendingMatchIfAbsent(AppUser swiper, AppUser swiped) {
        boolean exists = matchRepository
                .findByUser1IdAndUser2IdOrUser2IdAndUser1Id(swiper.getId(), swiped.getId())
                .isPresent();
        if (!exists) {
            saveMatch(swiper, swiped, Match.MatchStatus.PENDING);
        }
    }

    private void upgradeOrCreateMatch(AppUser swiper, AppUser swiped, Match.MatchStatus status) {
        Optional<Match> existing = matchRepository
                .findByUser1IdAndUser2IdOrUser2IdAndUser1Id(swiper.getId(), swiped.getId());
        if (existing.isPresent()) {
            Match match = existing.get();
            match.setStatus(status);
            match.setLastInteraction(LocalDateTime.now());
            matchRepository.save(match);
        } else {
            saveMatch(swiper, swiped, status);
        }
    }

    private void saveMatch(AppUser user1, AppUser user2, Match.MatchStatus status) {
        double score = compatibilityService.computeScore(user1, user2);
        Match match = new Match();
        match.setUser1(user1);
        match.setUser2(user2);
        match.setMatchScore(score);
        match.setStatus(status);
        match.setMatchedAt(LocalDateTime.now());
        match.setLastInteraction(LocalDateTime.now());
        matchRepository.save(match);
    }
}
