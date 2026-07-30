package com.apu.roommate_filter.repository;

import com.apu.roommate_filter.model.Match;
import com.apu.roommate_filter.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByUser1AndUser2(AppUser user1, AppUser user2);

    List<Match> findAllByUser1IdOrUser2Id(Long user1Id, Long user2Id);

    @Query("SELECT m FROM Match m WHERE " +
            "(m.user1.id = :userId OR m.user2.id = :userId) AND " +
            "m.status = 'ACCEPTED'")
    List<Match> findAllAcceptedMatches(@Param("userId") Long userId);

    @Query("SELECT m FROM Match m WHERE " +
            "m.user2.id = :userId AND " +
            "m.status = 'PENDING'")
    List<Match> findPendingMatchesWhereUserIsUser2(@Param("userId") Long userId);

    @Query("SELECT m FROM Match m WHERE " +
            "(m.user1.id = :user1Id AND m.user2.id = :user2Id) OR " +
            "(m.user1.id = :user2Id AND m.user2.id = :user1Id)")
    Optional<Match> findByUser1IdAndUser2IdOrUser2IdAndUser1Id(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id);
}
