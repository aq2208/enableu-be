package com.capstone.enableu.custom.security;


import com.capstone.enableu.common.security.CustomUserDetails;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String username) {
        UserEntity user = userRepository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException("User not found/deleted!"));

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
