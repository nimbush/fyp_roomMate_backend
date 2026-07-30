package com.apu.roommate_filter.dto.users;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ImageUploadRequest {
    @NotNull
    private Long userId;

    private List<MultipartFile> images;
}
