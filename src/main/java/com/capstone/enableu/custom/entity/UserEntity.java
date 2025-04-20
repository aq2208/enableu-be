package com.capstone.enableu.custom.entity;

import com.capstone.enableu.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "user")
public class UserEntity extends BaseEntity {
    private String username;
    private String password;
    private String role;
    private String email;
    private String fullName;
    private String gender;
    private Date dob;
    private String phone;
    private String address;
    private String shortcutInfo;
    private String status;
    private String refreshToken;
    private String title;
    private String links;
    private String additionalInfo;
    private String avatar;
}
