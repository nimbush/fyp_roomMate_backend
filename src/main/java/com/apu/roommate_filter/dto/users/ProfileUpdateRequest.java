package com.apu.roommate_filter.dto.users;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ProfileUpdateRequest {
    private Long id;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phoneNumber;

    private boolean student;

    @Size(max = 100)
    private String studyLevel;

    @Size(max = 150)
    private String occupation;

    private boolean searchingRoommate;
    private boolean searchingRoom;

    @Size(max = 50)
    private String roommateGender;

    @Size(max = 2000)
    private String propertyDescription;

    @Size(max = 1000)
    private String notes;

    @Min(0)
    private double minBudget;

    @Min(0)
    private double maxBudget;

    @Size(max = 50)
    private String sleepingSchedule;

    @Min(0)
    @Max(10)
    private int cleanliness;

    private boolean smoking;

    @Size(max = 50)
    private String diet;

    @Size(max = 500)
    private String hobbies;

    @Size(max = 100)
    private String musicPreference;

    @Size(max = 100)
    private String petPreference;

    private boolean preferredMale;
    private boolean preferredFemale;
    private List<MultipartFile> images;
}
