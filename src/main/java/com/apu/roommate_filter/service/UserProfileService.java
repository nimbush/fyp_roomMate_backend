package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.util.ImageDto;
import com.apu.roommate_filter.dto.users.ProfileUpdateRequest;
import com.apu.roommate_filter.dto.users.UserProfileResponse;
import com.apu.roommate_filter.mapper.AppUserMapper;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final String ROOMMATE_IMAGE_NOTE =
            "Images of the room along with your image are required.";

    private final AppUserRepository appUserRepository;
    private final AppUserMapper appUserMapper;
    private final ImageService imageService;

    public ResponseEntity<?> getUserProfile(Long userId) throws BadRequestException {
        AppUser appUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        long imageCount = imageService.countImagesForUser(userId);
        List<ImageDto> images = imageService.getImagesForUser(userId);
        UserProfileResponse profileResponse = appUserMapper.toProfileResponse(appUser, imageCount, images);

        return ResponseEntity.ok(
                Map.of(
                        "profileCompleted", appUser.isProfileCompleted(),
                        "data", profileResponse
                )
        );
    }

    public ResponseEntity<?> updateProfile(ProfileUpdateRequest request) throws IOException {
        if (request.getId() == null) {
            throw new ResponseStatusException(BAD_REQUEST, "User id is required.");
        }

        AppUser appUser = appUserRepository.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "User not found"));

        applyProfileUpdates(appUser, request);
        imageService.saveImages(appUser, request.getImages());

        long totalImageCount = imageService.countImagesForUser(appUser.getId());
        validateProfileRules(appUser, totalImageCount);

        appUser.setProfileCompleted(true);
        appUserRepository.save(appUser);

        List<ImageDto> images = imageService.getImagesForUser(appUser.getId());
        UserProfileResponse profileResponse = appUserMapper.toProfileResponse(appUser, totalImageCount, images);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "data", profileResponse
        ));
    }

    private void applyProfileUpdates(AppUser appUser, ProfileUpdateRequest request) {
        appUser.setFirstName(request.getFirstName());
        appUser.setLastName(request.getLastName());
        appUser.setPhoneNumber(request.getPhoneNumber());
        appUser.setStudent(request.isStudent());
        appUser.setStudyLevel(request.getStudyLevel());
        appUser.setOccupation(request.getOccupation());
        appUser.setSearchingRoommate(request.isSearchingRoommate());
        appUser.setSearchingRoom(request.isSearchingRoom());
        appUser.setRoommateGender(request.getRoommateGender());
        appUser.setPropertyDescription(request.getPropertyDescription());
        appUser.setNotes(request.getNotes());
        appUser.setMinBudget(request.getMinBudget());
        appUser.setMaxBudget(request.getMaxBudget());
        appUser.setSleepingSchedule(request.getSleepingSchedule());
        appUser.setCleanliness(request.getCleanliness());
        appUser.setSmoking(request.isSmoking());
        appUser.setDiet(request.getDiet());
        appUser.setHobbies(request.getHobbies());
        appUser.setMusicPreference(request.getMusicPreference());
        appUser.setPetPreference(request.getPetPreference());
        appUser.setPreferredMale(request.isPreferredMale());
        appUser.setPreferredFemale(request.isPreferredFemale());
    }

    private void validateProfileRules(AppUser appUser, long imageCount) {
        if (appUser.getMaxBudget() > 0 && appUser.getMinBudget() > appUser.getMaxBudget()) {
            throw new ResponseStatusException(BAD_REQUEST, "Minimum budget cannot be greater than maximum budget.");
        }

        if (appUser.isSearchingRoommate()) {
            if (isBlank(appUser.getPropertyDescription())) {
                throw new ResponseStatusException(BAD_REQUEST, "Property description is required when searching for a roommate.");
            }
            if (imageCount < 4) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "At least 4 images are required when searching for a roommate. " + ROOMMATE_IMAGE_NOTE
                );
            }
            if (isBlank(appUser.getNotes())) {
                appUser.setNotes(ROOMMATE_IMAGE_NOTE);
            }
        }

        if (appUser.isSearchingRoom() && imageCount < 1) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "At least 1 image is required when looking for a room with a roommate."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
