package com.capstone.enableu.custom.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface OtpService {
    String generateOtpAndSendSms(String phoneNumber) throws NoSuchAlgorithmException, IOException;
    String getOtp(String phoneNumber) throws NoSuchAlgorithmException;
    boolean verifyOtp(String phoneNumber, String otp) throws NoSuchAlgorithmException;

}
