package com.glm.payment;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-IP throttle for the public ToyyibPay callback/return endpoints, which are CSRF-exempt and
 * unauthenticated (server-to-server webhook + browser redirect). Mirrors LoginThrottleService's
 * Caffeine-backed counter pattern.
 */
@Component
public class CallbackRateLimiter {

    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    private final Cache<String, AtomicInteger> requests = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public boolean allow(String clientIp) {
        int count = requests.asMap()
                .computeIfAbsent(clientIp, k -> new AtomicInteger(0))
                .incrementAndGet();
        return count <= MAX_REQUESTS_PER_MINUTE;
    }
}
