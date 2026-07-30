package com.apu.roommate_filter.dto.users;

import com.apu.roommate_filter.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceContentResponse {
    private Long id;
    private ServiceType service;
    private String title;
    private String content;
}
