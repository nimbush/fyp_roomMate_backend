package com.apu.roommate_filter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Swipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "swiper_id")
    private AppUser swiper;

    @ManyToOne
    @JoinColumn(name = "swiped_id")
    private AppUser swiped;

    private boolean isLiked;
    private LocalDateTime swipedAt;
}
