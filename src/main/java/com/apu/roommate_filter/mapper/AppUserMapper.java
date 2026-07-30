package com.apu.roommate_filter.mapper;

import com.apu.roommate_filter.dto.util.ImageDto;
import com.apu.roommate_filter.dto.users.UserProfileResponse;
import com.apu.roommate_filter.model.AppUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppUserMapper {

    public UserProfileResponse toProfileResponse(AppUser appUser, long imageCount, List<ImageDto> images) {
        if (appUser == null) return null;

        return UserProfileResponse.builder()
                .id(appUser.getId())
                .firstName(appUser.getFirstName())
                .lastName(appUser.getLastName())
                .fullName(appUser.getFullName())
                .email(appUser.getEmail())
                .phoneNumber(appUser.getPhoneNumber())
                .student(appUser.isStudent())
                .studyLevel(appUser.getStudyLevel())
                .occupation(appUser.getOccupation())
                .minBudget(appUser.getMinBudget())
                .maxBudget(appUser.getMaxBudget())
                .sleepingSchedule(appUser.getSleepingSchedule())
                .cleanliness(appUser.getCleanliness())
                .smoking(appUser.isSmoking())
                .diet(appUser.getDiet())
                .hobbies(appUser.getHobbies())
                .musicPreference(appUser.getMusicPreference())
                .petPreference(appUser.getPetPreference())
                .preferredMale(appUser.isPreferredMale())
                .preferredFemale(appUser.isPreferredFemale())
                .searchingRoommate(appUser.isSearchingRoommate())
                .searchingRoom(appUser.isSearchingRoom())
                .roommateGender(appUser.getRoommateGender())
                .propertyDescription(appUser.getPropertyDescription())
                .notes(appUser.getNotes())
                .imageCount(imageCount)
                .images(images)
                .profileCompleted(appUser.isProfileCompleted())
                .verified(appUser.isVerified())
                .build();
    }
}
