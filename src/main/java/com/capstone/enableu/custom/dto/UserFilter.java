package com.capstone.enableu.custom.dto;

import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.enums.UserStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFilter {
    private List<UserStatus> status;
    private List<Role> role;
}
