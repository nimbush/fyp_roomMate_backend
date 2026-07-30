package com.apu.roommate_filter.dto.authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCredReq {
    private String email;
    private String password;
}
