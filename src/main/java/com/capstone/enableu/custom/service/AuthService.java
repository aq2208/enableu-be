package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.UserStatus;
import com.capstone.enableu.custom.exception.InvalidTokenException;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.UserRepository;
import com.capstone.enableu.custom.response.AuthResponse;
import com.capstone.enableu.custom.security.JwtTokenProvider;
import com.capstone.enableu.custom.util.Validate;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class AuthService extends BaseService<UserEntity, UserRepository> {
    private final JwtTokenProvider jwtUtil;

    public AuthService(JwtTokenProvider jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse login(String username, String password) {
        username = Validate.getUsername(username);
        // For simplicity, assuming static username and password check. Replace with actual user validation.
        UserEntity userEntity = repository.findByUsernameAndIsDeletedFalseAndStatusNotContaining(username, UserStatus.UNVERIFIED.toString())
                .orElseThrow(() -> new NotFoundException(ResponseMessage.USER_NOT_FOUND.name()));
        if (!BCrypt.checkpw(password, userEntity.getPassword())) {
            throw new BadCredentialsException(ResponseMessage.LOGIN_FAILED.name());
        }

        if (!Objects.equals(userEntity.getStatus(), UserStatus.ACTIVE.toString())) {
            throw new NotFoundException(ResponseMessage.ACCOUNT_INACTIVE.name());
        }
        userEntity.setRefreshToken(jwtUtil.generateRefreshToken(userEntity.getId().toString(), username, userEntity.getRole(), false));
        save(userEntity);
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(jwtUtil.generateAccessToken(userEntity.getId().toString(), username, userEntity.getRole(), false));
        authResponse.setRefreshToken(jwtUtil.generateRefreshToken(userEntity.getId().toString(), username, userEntity.getRole(), false));
        return authResponse;
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            Map<String, String> tokenInfo = jwtUtil.getTokenClaims(refreshToken);
            AuthResponse authResponse = new AuthResponse();
            authResponse.setAccessToken(jwtUtil.generateAccessToken(tokenInfo.get("userId"), tokenInfo.get("username"), tokenInfo.get("role"), false));
            authResponse.setRefreshToken(refreshToken);
            return authResponse;
        }
        throw new InvalidTokenException(ResponseMessage.TOKEN_INVALID.name());
    }
}
