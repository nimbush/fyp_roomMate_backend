package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.users.ServiceContentResponse;
import com.apu.roommate_filter.model.ServiceType;
import com.apu.roommate_filter.service.ServiceContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceContentController {

    private final ServiceContentService serviceContentService;

    @GetMapping("/{service}")
    public ResponseEntity<ServiceContentResponse> getByService(@PathVariable ServiceType service) {
        return ResponseEntity.ok(serviceContentService.getByService(service));
    }
}
