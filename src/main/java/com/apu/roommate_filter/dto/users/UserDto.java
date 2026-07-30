package com.apu.roommate_filter.dto.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
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
    private boolean profileCompleted;
    private boolean verified;
}
