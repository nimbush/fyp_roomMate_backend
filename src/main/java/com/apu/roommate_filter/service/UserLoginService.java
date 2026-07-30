package com.apu.roommate_filter.service;

import com.apu.roommate_filter.dto.authentication.ForgotPasswordRequest;
import com.apu.roommate_filter.dto.authentication.RegisterReq;
import com.apu.roommate_filter.dto.authentication.ResetPasswordRequest;
import com.apu.roommate_filter.dto.authentication.UserCredReq;
import com.apu.roommate_filter.dto.authentication.VerifyReq;
import com.apu.roommate_filter.exception.RateLimitException;
import com.apu.roommate_filter.model.AppUser;
import com.apu.roommate_filter.repository.AppUserRepository;
import com.apu.roommate_filter.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLoginService {

    private final JwtUtil jwtUtil;
    private final AppUserRepository appUserRepository;
    private final EmailVerificationService emailVerificationService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder encoder;
    private final RateLimiterService rateLimiterService;

    @Transactional
    public ResponseEntity<?> register(RegisterReq registerReq) {
        try {
            rateLimiterService.checkOtpLimit(registerReq.getEmail());

            if (appUserRepository.existsByEmail(registerReq.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email already exists"));
            }

            AppUser appUser = new AppUser();
            appUser.setFirstName(registerReq.getFirstName());
            appUser.setLastName(registerReq.getLastName());
            appUser.setEmail(registerReq.getEmail());
            appUser.setPhoneNumber(registerReq.getPhoneNumber());
            appUser.setPassword(encoder.encode(registerReq.getPassword()));
            appUser.setVerified(false);
            appUser.setProfileCompleted(false);

            appUserRepository.save(appUser);

            // The OTP is written to Redis *before* the mail is dispatched, so a failed send does not
            // strand the account — the user can recover via /resend-otp. Registration therefore
            // succeeds even when SMTP is down, instead of committing the row and returning 500.
            boolean emailSent = true;
            try {
                emailVerificationService.sendVerification(appUser.getEmail());
            } catch (Exception e) {
                emailSent = false;
                log.error("Verification email could not be sent to {}", appUser.getEmail(), e);
            }

            return ResponseEntity.ok(Map.of(
                    "message", emailSent
                            ? "Registration successful. Please verify your email."
                            : "Registration successful, but the verification email is delayed. "
                              + "Use resend OTP in a moment.",
                    "userId", appUser.getId(),
                    "emailSent", emailSent
            ));

        } catch (RateLimitException e) {
            return ResponseEntity.status(429).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Registration failed. Please try again."));
        }
    }

    public ResponseEntity<?> authentication(VerifyReq verifyReq) {
        try {
            AppUser appUser = appUserRepository.findByEmail(verifyReq.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean verified = emailVerificationService.verifyCode(
                    verifyReq.getEmail(), verifyReq.getOtp());

            if (!verified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid OTP"));
            }

            appUser.setVerified(true);
            appUserRepository.save(appUser);

            String jwt = jwtUtil.generatedToken(appUser.getEmail(), appUser.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully",
                    "token", jwt,
                    "userId", appUser.getId()
            ));

        } catch (Exception e) {
            log.error("OTP verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Verification failed. Please try again."));
        }
    }

    public ResponseEntity<?> resendOtp(String email) {
        try {
            rateLimiterService.checkOtpLimit(email);

            AppUser appUser = appUserRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (appUser.isVerified()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Account is already verified"));
            }

            emailVerificationService.sendVerification(email);
            return ResponseEntity.ok(Map.of("message", "OTP resent successfully"));

        } catch (RateLimitException e) {
            return ResponseEntity.status(429).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Resend OTP failed for {}", email, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    public ResponseEntity<?> login(UserCredReq userCredReq) {
        try {
            rateLimiterService.checkLoginLimit(userCredReq.getEmail());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userCredReq.getEmail(), userCredReq.getPassword()));

            AppUser appUser = appUserRepository.findByEmail(userCredReq.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            rateLimiterService.resetLoginAttempts(userCredReq.getEmail());

            String jwt = jwtUtil.generatedToken(appUser.getEmail(), appUser.getId());

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "userId", appUser.getId()
            ));

        } catch (RateLimitException e) {
            return ResponseEntity.status(429).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Login failed for {}", userCredReq.getEmail(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid credentials"));
        }
    }

    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        try {
            rateLimiterService.checkOtpLimit(request.getEmail());

            appUserRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("No account found with that email"));

            emailVerificationService.sendPasswordResetOtp(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Password reset OTP sent to your email"));

        } catch (RateLimitException e) {
            return ResponseEntity.status(429).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Forgot password failed for {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        try {
            AppUser user = appUserRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("No account found with that email"));

            boolean valid = emailVerificationService.verifyPasswordResetCode(
                    request.getEmail(), request.getOtp());
            if (!valid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired OTP"));
            }

            user.setPassword(encoder.encode(request.getNewPassword()));
            appUserRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));

        } catch (RuntimeException e) {
            log.error("Password reset failed for {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
