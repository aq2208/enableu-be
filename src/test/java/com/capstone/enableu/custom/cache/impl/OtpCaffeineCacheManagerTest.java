package com.capstone.enableu.custom.cache.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.capstone.enableu.custom.cache.OtpCacheManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

class OtpCacheManagerTest {

    @Test
    public void testOtpCacheManager() throws InterruptedException {
        OtpCacheManager otpCacheManager = new OtpCaffeineCacheManager();

        String key = "user123";
        String otp = "456789";
        otpCacheManager.put(key, otp, 2L, TimeUnit.SECONDS);

        // Should retrieve the OTP immediately
        assertEquals(otp, otpCacheManager.get(key));

        // Wait for 3 seconds to let the OTP expire
        Thread.sleep(3000);

        // Should return null as the OTP has expired
        assertNull(otpCacheManager.get(key));
    }
}
