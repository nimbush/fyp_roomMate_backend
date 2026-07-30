package com.apu.roommate_filter.controller;

import com.apu.roommate_filter.dto.authentication.ForgotPasswordRequest;
import com.apu.roommate_filter.dto.authentication.RegisterReq;
import com.apu.roommate_filter.dto.authentication.ResendOtpRequest;
import com.apu.roommate_filter.dto.authentication.ResetPasswordRequest;
import com.apu.roommate_filter.dto.authentication.UserCredReq;
import com.apu.roommate_filter.dto.authentication.VerifyReq;
import com.apu.roommate_filter.service.UserLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LogInController {

    private final UserLoginService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq registerReq) {
        return userService.register(registerReq);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyReq verifyReq) {
        return userService.authentication(verifyReq);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return userService.resendOtp(request.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserCredReq userCredReq) {
        return userService.login(userCredReq);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return userService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userService.resetPassword(request);
    }
}
