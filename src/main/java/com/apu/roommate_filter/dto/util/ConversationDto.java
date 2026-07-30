package com.apu.roommate_filter.dto.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private Long matchId;
    private Long otherUserId;
    private String otherUserName;
    private double matchScore;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
