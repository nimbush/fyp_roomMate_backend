package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.users.DiscoveryCandidateDto;
import com.apu.roommate_filter.service.DiscoveryService;
import com.apu.roommate_filter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    @GetMapping
    public ResponseEntity<?> getDiscoveryFeed(@RequestParam Long userId) {
        if (!SecurityUtils.getCallerUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        try {
            List<DiscoveryCandidateDto> feed = discoveryService.getDiscoveryFeed(userId);
            return ResponseEntity.ok(feed);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
