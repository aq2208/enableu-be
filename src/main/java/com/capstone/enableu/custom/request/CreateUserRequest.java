package com.capstone.enableu.custom.request;

import com.capstone.enableu.custom.dto.ShortcutInfo;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.UserStatus;
import com.capstone.enableu.custom.exception.BadRequestException;
import com.capstone.enableu.custom.response.UserResponse;
import com.capstone.enableu.custom.util.*;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CreateUserRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
    private String email;
    @NotNull
    private String fullName;
    private String gender;
    private String dob;
    private String address;
    @NotNull
    private String role;

    public String getUsername() {
        return Validate.getUsername(this.username);
    }

    public static UserEntity toEntity(CreateUserRequest userRequest) {
        UserEntity userEntity = new UserEntity();
        String username = userRequest.getUsername();
        if (!Validate.isNumeric(username)) {
            throw new BadRequestException(ResponseMessage.USER_INVALID_PHONE_NUMBER.toString());
        }
        userEntity.setUsername(username);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());
        userEntity.setPassword(hashedPassword);

        userEntity.setEmail(userRequest.getEmail());
        userEntity.setFullName(userRequest.getFullName());
        userEntity.setAddress(userRequest.getAddress());
        userEntity.setRole(userRequest.getRole());
        userEntity.setPhone(userRequest.getUsername());
        userEntity.setGender(userRequest.getGender());
        userEntity.setStatus(UserStatus.UNVERIFIED.toString());
        List<ShortcutInfo> shortcutInfoList = ShortcutInfo.defaultShortcutInfos();
        String shortcutJson = new Gson().toJson(shortcutInfoList);
        userEntity.setShortcutInfo(shortcutJson);

        userEntity.setDob(Validate.ConvertStringToDate(userRequest.getDob()));
        return userEntity;
    }
}
