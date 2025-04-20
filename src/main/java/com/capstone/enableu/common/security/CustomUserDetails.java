package com.capstone.enableu.common.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private Long userId;
    private String username;
    private String password;
    private String role;
    private Collection<? extends GrantedAuthority> authorities;
}
