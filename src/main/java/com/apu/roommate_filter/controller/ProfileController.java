package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.users.ProfileUpdateRequest;
import com.apu.roommate_filter.service.UserProfileService;
import com.apu.roommate_filter.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Validated
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<?> getUserProfile(@RequestParam Long userId) throws Exception {
        if (!SecurityUtils.getCallerUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return userProfileService.getUserProfile(userId);
    }

    @PutMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute ProfileUpdateRequest request) throws Exception {
        if (!SecurityUtils.getCallerUserId().equals(request.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return userProfileService.updateProfile(request);
    }
}
