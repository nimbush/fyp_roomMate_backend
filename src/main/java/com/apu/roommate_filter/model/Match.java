package com.apu.roommate_filter.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", referencedColumnName = "id")
    private AppUser user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", referencedColumnName = "id")
    private AppUser user2;

    private double matchScore; // Percentage score from matching algorithm

    @Enumerated(EnumType.STRING)
    private MatchStatus status; // PENDING, ACCEPTED, REJECTED

    private LocalDateTime matchedAt;
    private LocalDateTime lastInteraction;

    public enum MatchStatus {
        PENDING,  // One user has swiped right, waiting for other
        ACCEPTED, // Both users have swiped right
        REJECTED  // One user has explicitly rejected the match
    }
}
