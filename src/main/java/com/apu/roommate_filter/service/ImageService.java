package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.util.ImageDto;
import com.apu.roommate_filter.dto.users.ImageUploadRequest;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.model.Image;
import com.apu.roommate_filter.repository.AppUserRepository;
import com.apu.roommate_filter.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    /** Path prefix under which uploaded images are served (see ImageController). */
    private static final String IMAGE_URL_PREFIX = "/images/";

    private final ImageRepository imageRepository;
    private final AppUserRepository appUserRepository;

    @Value("${app.image.folder}")
    private String folderPath;

    @Transactional
    public String updateUserImages(ImageUploadRequest request) throws IOException {
        AppUser appUser = appUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Image> currentImages = imageRepository.findAllByUserId(appUser.getId());
        deleteImages(currentImages);

        List<Image> savedImages = saveImages(appUser, request.getImages());
        return savedImages.size() + " image(s) saved successfully";
    }

    @Transactional
    public List<Image> saveImages(AppUser appUser, List<MultipartFile> files) throws IOException {
        List<Image> savedImages = new ArrayList<>();
        if (files == null) return savedImages;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            savedImages.add(saveImage(appUser, file));
        }
        return savedImages;
    }

    public long countImagesForUser(Long userId) {
        return imageRepository.countByUserId(userId);
    }

    public List<ImageDto> getImagesForUser(Long userId) {
        List<Image> images = imageRepository.findAllByUserId(userId);
        if (images.isEmpty()) return Collections.emptyList();

        return images.stream()
                .map(image -> ImageDto.builder()
                        .id(image.getId())
                        .name(image.getImageName())
                        // Expose the retrieval URL, not the server filesystem path. The old value
                        // leaked absolute server paths and was unusable by the client.
                        .imageData(IMAGE_URL_PREFIX + image.getImageName())
                        .type(image.getImageType())
                        .build())
                .collect(Collectors.toList());
    }

    /** An image's bytes together with the content type to serve it under. */
    public record StoredImage(byte[] bytes, String contentType) {}

    public StoredImage loadImage(String name) throws IOException {
        Image image = imageRepository.findByImageName(name)
                .orElseThrow(() -> new RuntimeException("Image not found: " + name));

        Path path = resolveStoredPath(image);
        if (!Files.isReadable(path)) {
            throw new RuntimeException("Image file missing on disk: " + name);
        }

        String contentType = image.getImageType() == null || image.getImageType().isBlank()
                ? "application/octet-stream"
                : image.getImageType();

        return new StoredImage(Files.readAllBytes(path), contentType);
    }

    /**
     * `imageData` holds the absolute path recorded at upload time. If the app now runs with a
     * different `app.image.folder` (e.g. rows written on Windows, served from Linux/Docker), fall
     * back to locating the file by its stored name inside the configured folder.
     */
    private Path resolveStoredPath(Image image) {
        if (image.getImageData() != null && !image.getImageData().isBlank()) {
            Path recorded = Path.of(image.getImageData());
            if (Files.isReadable(recorded)) return recorded;
        }
        return resolveImageDirectory().resolve(image.getImageName()).normalize();
    }

    private void deleteImages(List<Image> images) throws IOException {
        for (Image image : images) {
            if (image.getImageData() != null && !image.getImageData().isBlank()) {
                Files.deleteIfExists(Path.of(image.getImageData()));
            }
        }
        if (!images.isEmpty()) {
            imageRepository.deleteAll(images);
        }
    }

    private Image saveImage(AppUser appUser, MultipartFile file) throws IOException {
        Path imageDirectory = resolveImageDirectory();
        Files.createDirectories(imageDirectory);

        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "_" + originalName.replace(" ", "_");
        Path targetPath = imageDirectory.resolve(storedName).normalize();

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return imageRepository.save(Image.builder()
                .imageName(storedName)
                .imageType(file.getContentType())
                .imageData(targetPath.toString())
                .user(appUser)
                .build());
    }

    private Path resolveImageDirectory() {
        Path configuredPath = Paths.get(folderPath);
        return configuredPath.isAbsolute()
                ? configuredPath.normalize()
                : configuredPath.toAbsolutePath().normalize();
    }
}
