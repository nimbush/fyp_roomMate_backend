package com.apu.roommate_filter.dto.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwipeDto {
    private Long id;
    private Long swiperId;
    private Long swipedId;
    private boolean isLiked;
    private LocalDateTime swipedAt;
}
