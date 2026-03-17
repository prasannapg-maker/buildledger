package com.buildledger.iam.service;

import com.buildledger.iam.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter using Bucket4j.
 * Limits login attempts: 5 per minute per IP address.
 * For production, use a distributed cache (Redis) backed implementation.
 */
@Service
@Slf4j
public class RateLimitingService {

    @Value("${rate-limit.login.capacity:5}")
    private int capacity;

    @Value("${rate-limit.login.refill-duration-minutes:1}")
    private int refillDurationMinutes;

    // Per-IP bucket map (use Redis for multi-instance deployments)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket getBucketForIp(String ipAddress) {
        return buckets.computeIfAbsent(ipAddress, k -> {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(capacity)
                    .refillGreedy(capacity, Duration.ofMinutes(refillDurationMinutes))
                    .build();
            return Bucket.builder().addLimit(limit).build();
        });
    }

    /**
     * Check and consume a login attempt token for the given IP.
     *
     * @throws RateLimitExceededException if rate limit is exceeded.
     */
    public void checkLoginRateLimit(String ipAddress) {
        Bucket bucket = getBucketForIp(ipAddress);
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            throw new RateLimitExceededException(
                "Too many login attempts. Please wait " + refillDurationMinutes + " minute(s) before retrying."
            );
        }
    }

    /**
     * Returns the number of remaining tokens for a given IP.
     */
    public long getRemainingAttempts(String ipAddress) {
        return getBucketForIp(ipAddress).getAvailableTokens();
    }

    /**
     * Reset the bucket for a given IP (e.g., after successful login).
     */
    public void reset(String ipAddress) {
        buckets.remove(ipAddress);
    }
}
