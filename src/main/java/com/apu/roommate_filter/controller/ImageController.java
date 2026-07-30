package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.users.ImageUploadRequest;
import com.apu.roommate_filter.service.ImageService;
import com.apu.roommate_filter.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@Valid @ModelAttribute ImageUploadRequest request) throws IOException {
        if (!SecurityUtils.getCallerUserId().equals(request.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(imageService.updateUserImages(request));
    }

    /**
     * Serves an uploaded image by its stored name. Lookup goes through the database, so the
     * filename cannot be used to traverse outside the configured image folder.
     */
    @GetMapping("/images/{name}")
    public ResponseEntity<?> serveImage(@PathVariable String name) {
        try {
            ImageService.StoredImage image = imageService.loadImage(name);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.contentType()))
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                    .body(image.bytes());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Image not found"));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Could not read image"));
        }
    }
}
