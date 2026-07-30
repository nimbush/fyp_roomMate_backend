package com.apu.roommate_filter.repository;

import com.apu.roommate_filter.model.ServiceContent;
import com.apu.roommate_filter.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceContentRepository extends JpaRepository<ServiceContent, Long> {
    Optional<ServiceContent> findByService(ServiceType service);
}
