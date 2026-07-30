package com.apu.roommate_filter.mapper;

import com.apu.roommate_filter.dto.util.SwipeDto;
import com.apu.roommate_filter.model.Swipe;

public class SwipeMapper {

    public static SwipeDto toDTO(Swipe swipe) {
        return new SwipeDto(
                swipe.getId(),
                swipe.getSwiper().getId(),
                swipe.getSwiped().getId(),
                swipe.isLiked(),
                swipe.getSwipedAt()
        );
    }
}
