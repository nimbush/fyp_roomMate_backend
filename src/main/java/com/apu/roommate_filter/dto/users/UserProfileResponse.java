package com.apu.roommate_filter.dto.users;

import com.apu.roommate_filter.dto.util.ImageDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean student;
    private String studyLevel;
    private String occupation;
    private double minBudget;
    private double maxBudget;
    private String sleepingSchedule;
    private int cleanliness;
    private boolean smoking;
    private String diet;
    private String hobbies;
    private String musicPreference;
    private String petPreference;
    private boolean preferredMale;
    private boolean preferredFemale;
    private boolean searchingRoommate;
    private boolean searchingRoom;
    private String roommateGender;
    private String propertyDescription;
    private String notes;
    private long imageCount;
    private List<ImageDto> images;
    private boolean profileCompleted;
    private boolean verified;
}
