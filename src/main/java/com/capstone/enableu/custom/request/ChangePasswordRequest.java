package com.capstone.enableu.custom.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    String oldPassword;
    String newPassword;
    String confirmPassword;
}
