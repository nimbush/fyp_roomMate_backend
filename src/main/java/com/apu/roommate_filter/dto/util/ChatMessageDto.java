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
public class ChatMessageDto {
    private Long id;
    private Long matchId;
    private Long senderId;
    private String senderName;
    private String content;
    private String messageType;
    private LocalDateTime sentAt;
}
