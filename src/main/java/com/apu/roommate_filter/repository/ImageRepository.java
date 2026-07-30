package com.apu.roommate_filter.repository;

import com.apu.roommate_filter.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImageName(String imageName);
    Optional<Image> findByUserId(Long userId);
    List<Image> findAllByUserId(Long userId);
    long countByUserId(Long userId);
    void deleteByUserId(Long userId);
}
