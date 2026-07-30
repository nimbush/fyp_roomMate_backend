package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.util.ChatMessageDto;
import com.apu.roommate_filter.dto.util.ConversationDto;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.model.ChatMessage;
import com.apu.roommate_filter.model.Match;
import com.apu.roommate_filter.repository.AppUserRepository;
import com.apu.roommate_filter.repository.ChatMessageRepository;
import com.apu.roommate_filter.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final MatchRepository matchRepository;
    private final AppUserRepository appUserRepository;

    @Transactional
    public ChatMessageDto saveMessage(Long matchId, Long senderId, String content, String messageType) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        requireParticipant(match, senderId);

        AppUser sender = appUserRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        ChatMessage msg = ChatMessage.builder()
                .match(match)
                .sender(sender)
                .content(content)
                .messageType(messageType)
                .sentAt(LocalDateTime.now())
                .build();

        chatMessageRepository.save(msg);

        match.setLastInteraction(msg.getSentAt());
        matchRepository.save(match);

        return toDto(msg);
    }

    public List<ChatMessageDto> getHistory(Long matchId, Long callerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        requireParticipant(match, callerId);
        return chatMessageRepository.findByMatchIdOrderBySentAtAsc(matchId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private void requireParticipant(Match match, Long callerId) {
        boolean isParticipant = match.getUser1().getId().equals(callerId)
                || match.getUser2().getId().equals(callerId);
        if (!isParticipant) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not a participant in this match");
        }
    }

    public List<ConversationDto> getConversations(Long userId) {
        List<Match> matches = matchRepository.findAllAcceptedMatches(userId);

        return matches.stream()
                .map(match -> {
                    AppUser other = match.getUser1().getId().equals(userId)
                            ? match.getUser2()
                            : match.getUser1();

                    Optional<ChatMessage> latest = chatMessageRepository.findLatestByMatchId(match.getId());

                    return ConversationDto.builder()
                            .matchId(match.getId())
                            .otherUserId(other.getId())
                            .otherUserName(other.getFullName())
                            .matchScore(match.getMatchScore())
                            .lastMessage(latest.map(ChatMessage::getContent).orElse(null))
                            .lastMessageAt(latest.map(ChatMessage::getSentAt).orElse(match.getMatchedAt()))
                            .build();
                })
                .sorted((a, b) -> {
                    if (a.getLastMessageAt() == null) return 1;
                    if (b.getLastMessageAt() == null) return -1;
                    return b.getLastMessageAt().compareTo(a.getLastMessageAt());
                })
                .toList();
    }

    private ChatMessageDto toDto(ChatMessage msg) {
        return ChatMessageDto.builder()
                .id(msg.getId())
                .matchId(msg.getMatch().getId())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getFullName())
                .content(msg.getContent())
                .messageType(msg.getMessageType())
                .sentAt(msg.getSentAt())
                .build();
    }
}
