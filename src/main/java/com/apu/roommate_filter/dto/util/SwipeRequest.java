package com.apu.roommate_filter.dto.util;

import lombok.Data;

@Data
public class SwipeRequest {
    private Long swiperId;
    private Long swipedId;
    private boolean liked;
}
