package com.apu.roommate_filter.dto.users;

import com.apu.roommate_filter.dto.util.ImageDto;
import lombok.Data;

import java.util.List;

@Data
public class DiscoveryCandidateDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String roommateGender;
    private double minBudget;
    private double maxBudget;
    private String occupation;
    private boolean student;
    private String studyLevel;
    private String sleepingSchedule;
    private int cleanliness;
    private boolean smoking;
    private String diet;
    private String hobbies;
    private String musicPreference;
    private String petPreference;
    private boolean searchingRoommate;
    private boolean searchingRoom;
    private String propertyDescription;
    private String notes;
    private double compatibilityScore;
    private List<ImageDto> images;
}
