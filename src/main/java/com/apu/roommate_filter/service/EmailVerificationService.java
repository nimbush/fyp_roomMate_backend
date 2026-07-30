package com.apu.roommate_filter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String CODE_PREFIX      = "2fa:";
    private static final String PWD_RESET_PREFIX = "pwd_reset:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final EmailService emailService;

    public void sendVerification(String email) {
        String code = generateCode();
        stringRedisTemplate.opsForValue().set(CODE_PREFIX + email, code, TTL);
        emailService.sendEmail(email, "Verification Code", code);
    }

    public boolean verifyCode(String email, String userEnteredCode) {
        String storedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (storedCode != null && storedCode.equals(userEnteredCode)) {
            // Single-use: consume the code so it cannot be replayed within its TTL.
            stringRedisTemplate.delete(CODE_PREFIX + email);
            return true;
        }
        return false;
    }

    public void sendPasswordResetOtp(String email) {
        String code = generateCode();
        stringRedisTemplate.opsForValue().set(PWD_RESET_PREFIX + email, code, TTL);
        emailService.sendEmail(email, "Password Reset Code", code);
    }

    public boolean verifyPasswordResetCode(String email, String code) {
        String stored = stringRedisTemplate.opsForValue().get(PWD_RESET_PREFIX + email);
        if (stored != null && stored.equals(code)) {
            stringRedisTemplate.delete(PWD_RESET_PREFIX + email);
            return true;
        }
        return false;
    }

    private String generateCode() {
        return String.format("%06d", 100000 + new Random().nextInt(900000));
    }
}
