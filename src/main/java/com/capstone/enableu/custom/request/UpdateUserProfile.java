package com.capstone.enableu.custom.request;

import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.enums.UserStatus;
import com.capstone.enableu.custom.util.Validate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateUserProfile {
    private String email;
    private String fullName;
    private String gender;
    private String dob;
    private String address;
    private String additionalInfo;
    private String title;
    private String links;

    public UserEntity updateUserProfile(UserEntity userEntity) {
       userEntity.setEmail(email);
       userEntity.setFullName(fullName);
       userEntity.setGender(gender);
       userEntity.setDob(Validate.ConvertStringToDate(dob));
       userEntity.setAddress(address);
       userEntity.setAdditionalInfo(additionalInfo);
       userEntity.setTitle(title);
       userEntity.setLinks(links);
       return userEntity;
    }
}
