package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.users.HelpSupportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpSupportService {

    private final EmailService emailService;

    public ResponseEntity<?> submit(HelpSupportRequest request) {
        try {
            emailService.sendSupportEmail(request);
        } catch (Exception e) {
            // A mail outage is a transient dependency failure, not an auth failure — say so
            // instead of letting the exception escape and surface to the client as 401.
            log.error("Help and support email failed for {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "error", "We could not send your request right now. Please try again later."
            ));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Help and support request sent successfully."
        ));
    }
}
