package com.apu.roommate_filter.repository;

import com.apu.roommate_filter.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByMatchIdOrderBySentAtAsc(Long matchId);

    // Latest message per match — used to build the conversation inbox
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.match.id = :matchId " +
           "ORDER BY cm.sentAt DESC LIMIT 1")
    Optional<ChatMessage> findLatestByMatchId(@Param("matchId") Long matchId);
}
