package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.users.ServiceContentResponse;
import com.apu.roommate_filter.model.ServiceContent;
import com.apu.roommate_filter.model.ServiceType;
import com.apu.roommate_filter.repository.ServiceContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ServiceContentService {

    private final ServiceContentRepository serviceContentRepository;

    public ServiceContentResponse getByService(ServiceType service) {
        ServiceContent serviceContent = serviceContentRepository.findByService(service)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "Content not found for service: " + service
                ));

        return new ServiceContentResponse(
                serviceContent.getId(),
                serviceContent.getService(),
                serviceContent.getTitle(),
                serviceContent.getContent()
        );
    }
}
