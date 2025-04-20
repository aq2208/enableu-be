package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.common.resolver.BaseResolver;
import com.capstone.enableu.custom.response.AuthResponse;
import com.capstone.enableu.custom.service.AuthService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class AuthResolver  extends BaseResolver<AuthService> {
    @MutationMapping(name = "login")
    public AuthResponse login(@Argument("username") String username, @Argument("password") String password) {
        return service.login(username, password);
    }

    @MutationMapping(name = "refresh")
    public AuthResponse refresh(@Argument("refreshToken") String refreshToken) {
        return service.refreshToken(refreshToken);
    }
}
