package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.users.HelpSupportRequest;
import com.apu.roommate_filter.service.HelpSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class HelpSupportController {

    private final HelpSupportService helpSupportService;

    @PostMapping
    public ResponseEntity<?> submit(@Valid @RequestBody HelpSupportRequest request) {
        return helpSupportService.submit(request);
    }
}
