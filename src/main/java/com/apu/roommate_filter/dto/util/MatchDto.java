package com.apu.roommate_filter.dto.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchDto {
    private Long id;
    private Long user1Id;
    private Long user2Id;
    /** The participant who is *not* the caller, so the client can label the row without a lookup. */
    private Long otherUserId;
    private String otherUserName;
    private String otherUserImage;
    private double matchScore;
    private String status;
    private LocalDateTime matchedAt;
    private LocalDateTime lastInteraction;
}
