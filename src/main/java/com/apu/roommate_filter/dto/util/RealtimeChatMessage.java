package com.apu.roommate_filter.dto.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeChatMessage {
    private Long matchId;
    private Long senderId;
    private String content;
    private String messageType;
    private LocalDateTime sentAt;
}
