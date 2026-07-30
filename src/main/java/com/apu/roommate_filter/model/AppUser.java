package com.apu.roommate_filter.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private String phoneNumber;

    private boolean student;
    private String studyLevel;
    private String occupation;
    private boolean profileCompleted;

    @Column(nullable = false)
    private boolean verified;

    private boolean searchingRoommate;
    private boolean searchingRoom;
    private String roommateGender;
    @Column(length = 2000)
    private String propertyDescription;
    @Column(length = 1000)
    private String notes;

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

    public String getFullName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        return (first + " " + last).trim();
    }
}
