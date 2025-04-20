package com.capstone.enableu.custom.service.impl;

import com.capstone.enableu.custom.cache.OtpCacheManager;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.enums.UserStatus;
import com.capstone.enableu.custom.service.OtpService;
import com.capstone.enableu.custom.service.SmsService;
import com.capstone.enableu.custom.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpCacheManager otpCacheManager;
    private final SmsService smsService;
    private final UserService userService;

    private static final long OTP_EXPIRY_DURATION = 2L;
    private static final TimeUnit OTP_EXPIRY_UNIT = TimeUnit.MINUTES;
    private static final String ERROR = "error";
    private static final String SUCCESS = "success";


    @Override
    public String generateOtpAndSendSms(String phoneNumber) throws NoSuchAlgorithmException, IOException {
        // generate random 6 digit otp
        SecureRandom random = new SecureRandom();
        int otpNumber = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpNumber);

        // init key by user signature
        String key = "OTP_" + getUserSignature(phoneNumber);
        otpCacheManager.put(key, otp, OTP_EXPIRY_DURATION, OTP_EXPIRY_UNIT);

        // send OTP to user
        String response = smsService.sendSMS(phoneNumber, otp);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        String status = jsonNode.get("status").asText();
        if (status.equals(ERROR)) {
            // delete account phone
            if (phoneNumber.startsWith("0")) {
                phoneNumber = phoneNumber.replaceFirst("0", "");
            }
            UserEntity user = userService.findByUsernameAndNotDeleted(phoneNumber);
            userService.softDelete(user.getId());
        }
        return response;
    }

    @Override
    public String getOtp(String phoneNumber) throws NoSuchAlgorithmException {
        String key = "OTP_" + getUserSignature(phoneNumber);
        return otpCacheManager.get(key);
    }

    // generate user signature
    private String getUserSignature(String phoneNumber) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(phoneNumber.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) throws NoSuchAlgorithmException {
        String otpKey = "OTP_" + getUserSignature(phoneNumber);
        String cachedOtp = getOtp(phoneNumber);
        // Check if OTP exists (not expired) and matches the provided OTP
        if (cachedOtp != null && cachedOtp.equals(otp)) {
            // Optionally, remove OTP after successful verification
            otpCacheManager.put(otpKey, "", 0L, OTP_EXPIRY_UNIT);
            // change status of user to verified
            UserEntity user = userService.findByUsernameAndNotDeleted(phoneNumber);
            if (user.getRole().equals(Role.TRAINER.name())) {
                user.setStatus(UserStatus.PENDING.name());
            } else if (user.getRole().equals(Role.TRAINEE.name())) {
                user.setStatus(UserStatus.ACTIVE.name());
            }
            userService.update(user);
            return true;
        }
        return false;
    }

}
