package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.util.SwipeDto;
import com.apu.roommate_filter.dto.util.SwipeRequest;
import com.apu.roommate_filter.service.SwipeService;
import com.apu.roommate_filter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/swipes")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping
    public ResponseEntity<?> recordSwipe(@RequestBody SwipeRequest request) {
        if (!SecurityUtils.getCallerUserId().equals(request.getSwiperId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        try {
            SwipeDto result = swipeService.recordSwipe(request);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
