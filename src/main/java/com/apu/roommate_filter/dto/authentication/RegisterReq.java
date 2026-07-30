package com.apu.roommate_filter.dto.authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterReq {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
}
