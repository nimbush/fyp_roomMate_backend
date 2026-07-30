package com.apu.roommate_filter.dto.authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyReq {
    private String otp;
    private String email;
}
