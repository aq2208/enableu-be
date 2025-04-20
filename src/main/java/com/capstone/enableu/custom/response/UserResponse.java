package com.capstone.enableu.custom.response;

import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.enums.UserStatus;
import com.capstone.enableu.custom.util.Validate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String code;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private String phone;
    private String gender;
    private String dob;
    private String role;
    private UserStatus status;
    private String address;
    private String additionalInfo;
    private String title;
    private String links;
    private List<ShortcutInfo> shortcutInfo;
    @Getter
    @Setter
    public static class ShortcutInfo {
        private String name;
        private String keyboard;
    }
    private String createdAt;

    //fromEntities method
    public static List<UserResponse> fromEntities(List<UserEntity> userEntities) {
        List<UserResponse> userResponses = new ArrayList<>();
        if (userEntities != null) {
            userEntities.stream().map(UserResponse::fromEntity).forEach(userResponses::add);
        }
        return userResponses;
    }

    public static UserResponse fromEntity(UserEntity user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setCode(user.getCode().toString());
        userResponse.setUsername(user.getUsername());
        userResponse.setAddress(user.getAddress());
        userResponse.setStatus(UserStatus.valueOf(user.getStatus()));
        userResponse.setEmail(user.getEmail());
        userResponse.setFullName(user.getFullName());
        userResponse.setPhone(user.getPhone());
        userResponse.setGender(user.getGender());
        userResponse.setDob(Validate.convertDateToString(user.getDob()));
        userResponse.setAdditionalInfo(user.getAdditionalInfo());
        userResponse.setTitle(user.getTitle());
        userResponse.setLinks(user.getLinks());
        userResponse.setAvatar(user.getAvatar());
        userResponse.setRole(user.getRole());
        userResponse.setCreatedAt(Validate.convertDateToString(user.getCreatedTime()));

        Type listType = new TypeToken<List<ShortcutInfo>>() {}.getType();
        Gson gson = new Gson();
        List<ShortcutInfo> shortcutInfoList = gson.fromJson(user.getShortcutInfo(), listType);
        userResponse.setShortcutInfo(shortcutInfoList);

        return userResponse;
    }
}