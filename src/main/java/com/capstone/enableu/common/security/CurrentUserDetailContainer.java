package com.capstone.enableu.common.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserDetailContainer {

    public CustomUserDetails getCurrentUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            return null;
        }
        Authentication authentication = context.getAuthentication();
        if( authentication instanceof AnonymousAuthenticationToken){
            return null;
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
