package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.util.MatchDto;
import com.apu.roommate_filter.service.MatchService;
import com.apu.roommate_filter.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllMatchesForUser(@PathVariable Long userId) {
        if (!SecurityUtils.getCallerUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        List<MatchDto> matches = matchService.getAllMatchesForUser(userId);
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/user/{userId}/accepted")
    public ResponseEntity<?> getAcceptedMatches(@PathVariable Long userId) {
        if (!SecurityUtils.getCallerUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(matchService.getAcceptedMatches(userId));
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<?> getPendingMatches(@PathVariable Long userId) {
        if (!SecurityUtils.getCallerUserId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(matchService.getPendingMatchesForUser(userId));
    }

    @PutMapping("/accept/{matchId}")
    public ResponseEntity<?> acceptMatch(@PathVariable Long matchId) {
        try {
            Long callerId = SecurityUtils.getCallerUserId();
            return ResponseEntity.ok(matchService.acceptMatch(matchId, callerId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/reject/{matchId}")
    public ResponseEntity<?> rejectMatch(@PathVariable Long matchId) {
        try {
            Long callerId = SecurityUtils.getCallerUserId();
            return ResponseEntity.ok(matchService.rejectMatch(matchId, callerId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
