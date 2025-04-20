package com.capstone.enableu.custom.cache.impl;

import com.capstone.enableu.custom.cache.OtpCacheManager;
import com.github.benmanes.caffeine.cache.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class OtpCaffeineCacheManager implements OtpCacheManager {

    private final Cache<String, String> cache;
    private final ConcurrentHashMap<String, Long> expiryMap = new ConcurrentHashMap<>();
    private final Ticker ticker = Ticker.systemTicker();

    public OtpCaffeineCacheManager() {
        cache = Caffeine.newBuilder()
                .ticker(ticker)
                .expireAfter(new Expiry<String, String>() {
                    @Override
                    public long expireAfterCreate(String key, String value, long currentTime) {
                        return getRemainingTime(key, currentTime);
                    }

                    @Override
                    public long expireAfterUpdate(String key, String value, long currentTime, long currentDuration) {
                        return getRemainingTime(key, currentTime);
                    }

                    @Override
                    public long expireAfterRead(String key, String value, long currentTime, long currentDuration) {
                        // Do not modify the expiration time on read
                        return currentDuration;
                    }
                })
                .build();
    }

    @Override
    public String get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(String key, String value, Long expiredTime, TimeUnit timeUnit) {
        long currentTime = ticker.read();
        long expiryTime = currentTime + timeUnit.toNanos(expiredTime);
        expiryMap.put(key, expiryTime);
        cache.put(key, value);
    }

    private long getRemainingTime(String key, long currentTime) {
        Long expiryTime = expiryMap.get(key);
        if (expiryTime == null) {
            // If no expiry time is set, expire immediately
            return 0;
        } else {
            long remainingTime = expiryTime - currentTime;
            // Ensure the remaining time is not negative
            return Math.max(remainingTime, 0);
        }
    }
}
