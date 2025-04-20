package com.capstone.enableu.custom.security;

import com.capstone.enableu.common.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtTokenProvider jwtTokenProvider;

    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getJwtFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {

            // Check if the "needToReset" claim is true
            Boolean needToReset = (Boolean) jwtTokenProvider.getCheckResetClaim(token);

            // If true, only allow access to the password reset endpoint
            if (Boolean.TRUE.equals(needToReset) && !request.getRequestURI().equals("/api/users/reset-password")) {
                // TODO: handle need reset password
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Password reset required to use this feature");
                return;
            }

            Map<String, String> tokenClaims = jwtTokenProvider.getTokenClaims(token);
            String loginAccountId = tokenClaims.get("username");
            // TODO: handle roles

            CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(loginAccountId);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken
                            (
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

            SecurityContextHolder.getContext().setAuthentication(authentication);


        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
