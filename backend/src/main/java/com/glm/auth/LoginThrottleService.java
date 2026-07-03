package com.glm.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginThrottleService {

    private static final int MAX_FAILURES   = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final Cache<String, AtomicInteger> failures = Caffeine.newBuilder()
            .expireAfterWrite(LOCKOUT_MINUTES, TimeUnit.MINUTES)
            .build();

    public boolean isLocked(String email) {
        AtomicInteger count = failures.getIfPresent(email.toLowerCase());
        return count != null && count.get() >= MAX_FAILURES;
    }

    public void recordFailure(String email) {
        failures.asMap()
                .computeIfAbsent(email.toLowerCase(), k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    public void clearFailures(String email) {
        failures.invalidate(email.toLowerCase());
    }
}
