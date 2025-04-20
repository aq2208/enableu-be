package com.capstone.enableu.custom.cache;

import java.util.concurrent.TimeUnit;

public interface OtpCacheManager {
    String get(String key);
    void put(String key, String value, Long expiredTime, TimeUnit timeUnit);

}
