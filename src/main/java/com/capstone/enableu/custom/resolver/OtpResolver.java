package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.service.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Controller
@AllArgsConstructor
public class OtpResolver {

    private final OtpService otpService;

    @MutationMapping
//    @Secured(Role.TRAINEE_CONSTANT)
    public String generateOtpAndSendSms(@Argument String phoneNumber) throws NoSuchAlgorithmException, IOException {
        return otpService.generateOtpAndSendSms(phoneNumber);
    }

    @MutationMapping
//    @Secured(Role.TRAINEE_CONSTANT)
    public String verifyOtp(@Argument(name = "phoneNumber") String phoneNumber, @Argument(name = "otp") String otp) throws NoSuchAlgorithmException {
        boolean isVerified = otpService.verifyOtp(phoneNumber, otp);
        if (isVerified) {
            return ResponseMessage.OTP_VERIFIED.name();
        }
        return ResponseMessage.OTP_INVALID_OR_EXPIRED.name();

    }
}
