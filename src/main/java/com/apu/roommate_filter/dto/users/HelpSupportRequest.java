package com.apu.roommate_filter.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelpSupportRequest {
    @NotBlank
    @Size(max = 150)
    private String username;

    @NotBlank
    @Size(max = 20)
    private String phoneNumber;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    @Size(max = 5000)
    private String body;
}
