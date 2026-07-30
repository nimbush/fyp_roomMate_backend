package com.apu.roommate_filter.service;

import com.apu.roommate_filter.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private static final String OTP_PREFIX   = "rate:otp:";
    private static final String LOGIN_PREFIX = "rate:login:";
    private static final int MAX_OTP    = 3;
    private static final int MAX_LOGIN  = 5;
    private static final Duration OTP_WINDOW   = Duration.ofMinutes(15);
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public void checkOtpLimit(String email) {
        String key = keyFor(OTP_PREFIX, email);
        if (key == null) return; // no identifiable subject – let validation reject the request
        check(key, MAX_OTP, OTP_WINDOW,
                "Too many OTP requests. Please wait 15 minutes before trying again.");
    }

    public void checkLoginLimit(String email) {
        String key = keyFor(LOGIN_PREFIX, email);
        if (key == null) return;
        check(key, MAX_LOGIN, LOGIN_WINDOW,
                "Too many failed login attempts. Please wait 10 minutes before trying again.");
    }

    public void resetLoginAttempts(String email) {
        String key = keyFor(LOGIN_PREFIX, email);
        if (key != null) redisTemplate.delete(key);
    }

    /**
     * Builds a per-subject bucket key. A missing/blank email previously produced a shared
     * "rate:otp:null" bucket, letting one malformed client rate-limit every other one.
     */
    private String keyFor(String prefix, String email) {
        if (email == null || email.isBlank()) return null;
        return prefix + email.trim().toLowerCase();
    }

    private void check(String key, int max, Duration window, String message) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, window);
        }
        if (count != null && count > max) {
            throw new RateLimitException(message);
        }
    }
}
