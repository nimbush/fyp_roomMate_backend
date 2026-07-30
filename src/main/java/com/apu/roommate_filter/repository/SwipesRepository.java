package com.apu.roommate_filter.repository;

import com.apu.roommate_filter.model.Swipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwipesRepository extends JpaRepository<Swipe, Long> {
    Optional<Swipe> findBySwiperIdAndSwipedId(Long swiperId, Long swipedId);
    List<Swipe> findBySwiperId(Long swiperId);
}
