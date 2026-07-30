package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.util.SwipeRequest;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.model.Match;
import com.apu.roommate_filter.model.Swipe;
import com.apu.roommate_filter.repository.AppUserRepository;
import com.apu.roommate_filter.repository.MatchRepository;
import com.apu.roommate_filter.repository.SwipesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SwipeServiceTest {

    private SwipesRepository swipesRepository;
    private MatchRepository matchRepository;
    private AppUserRepository appUserRepository;
    private CompatibilityService compatibilityService;
    private SwipeService swipeService;

    private AppUser userA;
    private AppUser userB;

    @BeforeEach
    void setUp() {
        swipesRepository    = mock(SwipesRepository.class);
        matchRepository     = mock(MatchRepository.class);
        appUserRepository   = mock(AppUserRepository.class);
        compatibilityService = mock(CompatibilityService.class);
        swipeService = new SwipeService(swipesRepository, matchRepository, appUserRepository, compatibilityService);

        userA = new AppUser(); userA.setId(1L); userA.setFirstName("Alice");
        userB = new AppUser(); userB.setId(2L); userB.setFirstName("Bob");

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(userA));
        when(appUserRepository.findById(2L)).thenReturn(Optional.of(userB));
        when(compatibilityService.computeScore(any(), any())).thenReturn(75.0);
        when(swipesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void dislike_does_not_create_match() {
        SwipeRequest req = new SwipeRequest();
        req.setSwiperId(1L);
        req.setSwipedId(2L);
        req.setLiked(false);

        when(swipesRepository.findBySwiperIdAndSwipedId(1L, 2L)).thenReturn(Optional.empty());

        swipeService.recordSwipe(req);

        verify(matchRepository, never()).save(any());
    }

    @Test
    void first_like_creates_pending_match() {
        SwipeRequest req = new SwipeRequest();
        req.setSwiperId(1L);
        req.setSwipedId(2L);
        req.setLiked(true);

        when(swipesRepository.findBySwiperIdAndSwipedId(1L, 2L)).thenReturn(Optional.empty());
        when(swipesRepository.findBySwiperIdAndSwipedId(2L, 1L)).thenReturn(Optional.empty());
        when(matchRepository.findByUser1IdAndUser2IdOrUser2IdAndUser1Id(1L, 2L)).thenReturn(Optional.empty());

        swipeService.recordSwipe(req);

        verify(matchRepository).save(argThat(m -> m.getStatus() == Match.MatchStatus.PENDING));
    }

    @Test
    void mutual_like_creates_accepted_match() {
        SwipeRequest req = new SwipeRequest();
        req.setSwiperId(1L);
        req.setSwipedId(2L);
        req.setLiked(true);

        Swipe reciprocal = new Swipe();
        reciprocal.setSwiper(userB);
        reciprocal.setSwiped(userA);
        reciprocal.setLiked(true);

        when(swipesRepository.findBySwiperIdAndSwipedId(1L, 2L)).thenReturn(Optional.empty());
        when(swipesRepository.findBySwiperIdAndSwipedId(2L, 1L)).thenReturn(Optional.of(reciprocal));
        when(matchRepository.findByUser1IdAndUser2IdOrUser2IdAndUser1Id(1L, 2L)).thenReturn(Optional.empty());

        swipeService.recordSwipe(req);

        verify(matchRepository).save(argThat(m -> m.getStatus() == Match.MatchStatus.ACCEPTED));
    }

    @Test
    void mutual_like_upgrades_existing_pending_to_accepted() {
        SwipeRequest req = new SwipeRequest();
        req.setSwiperId(1L);
        req.setSwipedId(2L);
        req.setLiked(true);

        Swipe reciprocal = new Swipe();
        reciprocal.setSwiper(userB);
        reciprocal.setSwiped(userA);
        reciprocal.setLiked(true);

        Match pending = new Match();
        pending.setId(10L);
        pending.setUser1(userB);
        pending.setUser2(userA);
        pending.setStatus(Match.MatchStatus.PENDING);

        when(swipesRepository.findBySwiperIdAndSwipedId(1L, 2L)).thenReturn(Optional.empty());
        when(swipesRepository.findBySwiperIdAndSwipedId(2L, 1L)).thenReturn(Optional.of(reciprocal));
        when(matchRepository.findByUser1IdAndUser2IdOrUser2IdAndUser1Id(1L, 2L)).thenReturn(Optional.of(pending));

        swipeService.recordSwipe(req);

        verify(matchRepository).save(argThat(m -> m.getStatus() == Match.MatchStatus.ACCEPTED));
    }

    @Test
    void duplicate_swipe_throws_illegal_state() {
        SwipeRequest req = new SwipeRequest();
        req.setSwiperId(1L);
        req.setSwipedId(2L);
        req.setLiked(true);

        Swipe existing = new Swipe();
        when(swipesRepository.findBySwiperIdAndSwipedId(1L, 2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> swipeService.recordSwipe(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Already swiped");
    }

    @Test
    void self_swipe_is_rejected() {
        // Regression for BE-06: this used to succeed and create a match with user1 == user2.
        SwipeRequest req = new SwipeRequest();
        req.setSwiperId(1L);
        req.setSwipedId(1L);
        req.setLiked(true);

        assertThatThrownBy(() -> swipeService.recordSwipe(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot swipe on yourself");

        verify(swipesRepository, never()).save(any());
        verify(matchRepository, never()).save(any());
    }

    @Test
    void compatibility_score_stored_on_new_match() {
        SwipeRequest req = new SwipeRequest();
        req.setSwiperId(1L);
        req.setSwipedId(2L);
        req.setLiked(true);

        when(swipesRepository.findBySwiperIdAndSwipedId(1L, 2L)).thenReturn(Optional.empty());
        when(swipesRepository.findBySwiperIdAndSwipedId(2L, 1L)).thenReturn(Optional.empty());
        when(matchRepository.findByUser1IdAndUser2IdOrUser2IdAndUser1Id(1L, 2L)).thenReturn(Optional.empty());
        when(compatibilityService.computeScore(userA, userB)).thenReturn(88.5);

        swipeService.recordSwipe(req);

        verify(matchRepository).save(argThat(m -> m.getMatchScore() == 88.5));
    }
}
