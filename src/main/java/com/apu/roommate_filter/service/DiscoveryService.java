package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.users.DiscoveryCandidateDto;
import com.apu.roommate_filter.dto.util.ImageDto;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.repository.AppUserRepository;
import com.apu.roommate_filter.repository.SwipesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscoveryService {

    private final AppUserRepository appUserRepository;
    private final SwipesRepository swipesRepository;
    private final CompatibilityService compatibilityService;
    private final ImageService imageService;

    public List<DiscoveryCandidateDto> getDiscoveryFeed(Long userId) {
        AppUser currentUser = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<Long> alreadySwiped = swipesRepository.findBySwiperId(userId)
                .stream()
                .map(s -> s.getSwiped().getId())
                .collect(Collectors.toSet());

        return appUserRepository.findAllByProfileCompletedTrueAndVerifiedTrue()
                .stream()
                .filter(c -> !c.getId().equals(userId))
                .filter(c -> !alreadySwiped.contains(c.getId()))
                // Exclude on the gender rule itself. Filtering on `score > 0` also dropped users
                // who were allowed but simply scored zero, and hid the "ANY" bug (BE-11).
                .filter(c -> compatibilityService.isGenderCompatible(currentUser, c))
                .map(c -> toDto(c, compatibilityService.computeScore(currentUser, c)))
                .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                .toList();
    }

    private DiscoveryCandidateDto toDto(AppUser user, double score) {
        List<ImageDto> images = imageService.getImagesForUser(user.getId());

        DiscoveryCandidateDto dto = new DiscoveryCandidateDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRoommateGender(user.getRoommateGender());
        dto.setMinBudget(user.getMinBudget());
        dto.setMaxBudget(user.getMaxBudget());
        dto.setOccupation(user.getOccupation());
        dto.setStudent(user.isStudent());
        dto.setStudyLevel(user.getStudyLevel());
        dto.setSleepingSchedule(user.getSleepingSchedule());
        dto.setCleanliness(user.getCleanliness());
        dto.setSmoking(user.isSmoking());
        dto.setDiet(user.getDiet());
        dto.setHobbies(user.getHobbies());
        dto.setMusicPreference(user.getMusicPreference());
        dto.setPetPreference(user.getPetPreference());
        dto.setSearchingRoommate(user.isSearchingRoommate());
        dto.setSearchingRoom(user.isSearchingRoom());
        dto.setPropertyDescription(user.getPropertyDescription());
        dto.setNotes(user.getNotes());
        dto.setCompatibilityScore(score);
        dto.setImages(images);
        return dto;
    }
}
