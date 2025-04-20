package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.common.security.CustomUserDetails;
import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.enums.UserStatus;
import com.capstone.enableu.custom.exception.BadRequestException;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.UserRepository;
import com.capstone.enableu.custom.request.ChangePasswordRequest;
import com.capstone.enableu.custom.request.CreateUserRequest;
import com.capstone.enableu.custom.request.UpdateUserProfile;
import com.capstone.enableu.custom.dto.ShortcutInfo;
import com.capstone.enableu.custom.response.UserResponse;
import com.capstone.enableu.custom.dto.UserFilter;
import com.capstone.enableu.custom.util.Validate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
@AllArgsConstructor

public class UserService extends BaseService<UserEntity, UserRepository> {

    private final SmsService smsService;
    private final PasswordEncoder passwordEncoder;
    private final String ERROR = "error";

    public String ok() {
        return "healthy updated version 2";
    }
    private final Logger log = Logger.getLogger(this.getClass().getName());

    public UserEntity register(CreateUserRequest createUserRequest) {
        UserEntity u = repository.findByUsernameAndIsDeletedFalse(createUserRequest.getUsername())
                .orElse(null);
        UserEntity registerEntity = CreateUserRequest.toEntity(createUserRequest);
        if (Objects.isNull(u)) {
            return save(registerEntity);
        }
        if (Objects.equals(u.getStatus(), UserStatus.UNVERIFIED.toString())) {
            registerEntity.setId(u.getId());
            registerEntity.setCode(u.getCode());
            registerEntity.setCreatedTime(new Date());
            registerEntity.setUpdatedTime(new Date());
            return update(registerEntity);
        }
        throw new BadRequestException(ResponseMessage.USER_ALREADY_EXISTS.toString());
    }

    public List<Long> findUserIdsContainingFullName(String search) {
        return repository.findByFullNameIsContainingIgnoreCase(search)
                .stream().map(BaseEntity::getId).toList();
    }

    public UserEntity findByUsernameAndNotDeleted(String username) {
        return repository.findByUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString()));
    }

    @Transactional
    public UserEntity changePassword(ChangePasswordRequest request) {
        CustomUserDetails userDetails = getCurrentUser();
        UserEntity user = findByUsernameAndNotDeleted(userDetails.getUsername());
        if (!BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(ResponseMessage.INCORRECT_PASSWORD.toString());
        }
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new BadRequestException(ResponseMessage.INCORRECT_PASSWORD.toString());
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        return update(user);
    }

    @Transactional
    public String forgotPassword(String username) throws IOException {
        UserEntity user = findByUsernameAndNotDeleted(username);
        String randomRawPassword = String.valueOf((int) (Math.random() * 900000 + 100000));
        user.setPassword(passwordEncoder.encode(randomRawPassword));
        update(user);
        String response = smsService.sendSMS(user.getPhone(), randomRawPassword);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response);
        String status = jsonNode.get("status").asText();
        if (status.equals(ERROR)) {
            return ResponseMessage.PHONE_NUMBER_NOT_EXIST.name();
        }
        return ResponseMessage.CODE_SENT.name() + username;
    }

    public UserEntity updateStatus(Long id, String status) {
        UserEntity user = findByIdAndNotDeleted(id);
        if (user == null) {
            throw new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString());
        }
        user.setStatus(status);
        return update(user);
    }
//
    public UserResponse getUserDetail(Long id) {
        UserEntity userEntity = repository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString()));
        if (getCurrentUser().getRole().equals(Role.TRAINEE.name()) && !getCurrentUser().getUserId().equals(id)) {
            try {
                throw new AuthenticationException();
            } catch (AuthenticationException e) {
                throw new RuntimeException(e);
            }
        }
        return UserResponse.fromEntity(userEntity);
    }

    public UserResponse updateUserProfile(Long id, UpdateUserProfile updateUserProfile) {
        UserEntity userEntity = repository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString()));
        userEntity = updateUserProfile.updateUserProfile(userEntity);
        return UserResponse.fromEntity(update(userEntity));
    }

    public String updateUserAvatar(Long id, String avatar) {
        UserEntity userEntity = repository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString()));
        userEntity.setAvatar(avatar);
        update(userEntity);
        return ResponseMessage.USER_UPDATE_SUCCESS.toString();
    }

    public UserResponse updateShortcut(Long id, List<ShortcutInfo> shortcutInfo) {
        UserEntity userEntity = repository.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString()));
        String shortcutJson = new Gson().toJson(shortcutInfo);
        userEntity.setShortcutInfo(shortcutJson);
        update(userEntity);
        return UserResponse.fromEntity(userEntity);
    }
    public List<UserResponse> getAllActiveTrainee() {
        Long currentUserId = getCurrentUser().getUserId();
        return UserResponse.fromEntities(repository.findAllActiveTrainee(currentUserId));
    }

    public List<UserResponse> getAllActiveTraineeOfCategory(Long categoryId) {
        Long currentUserId = getCurrentUser().getUserId();
        return UserResponse.fromEntities(repository.findAllActiveTraineeOfCategory(categoryId, currentUserId));
    }

    public List<UserResponse> getAllActiveTraineeOfTask(Long taskId) {
        Long currentUserId = getCurrentUser().getUserId();
        return UserResponse.fromEntities(repository.findAllActiveTraineeOfTask(taskId, currentUserId));
    }


    public List<UserResponse> getUserList(UserFilter filter, String keyword) {
        String search = "";
        List<UserEntity> userList = new ArrayList<>();
        if (keyword != null) {
            search = keyword;
            if (Validate.isNumeric(keyword)) {
                search = Validate.getUsername(keyword);
            }
            userList = repository.findByIsDeletedFalseAndFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseAndIsDeletedFalse(search, search);
        } else {
            userList = repository.findByIsDeletedFalse();
        }
        if (filter == null) {
            return userList
                    .stream()
                    .map(UserResponse::fromEntity).toList();
        }
        Stream<UserEntity> filtered = userList
                .stream()
                .filter(userEntity -> Optional.of(filter)
                        .map(UserFilter::getStatus)
                        .map(userStatuses -> userStatuses.contains(UserStatus.valueOf(userEntity.getStatus())))
                        .orElse(true))
                .filter(userEntity -> Optional.of(filter)
                        .map(UserFilter::getRole)
                        .map(roles -> roles.contains(Role.valueOf(userEntity.getRole())))
                        .orElse(true));
        return filtered.map(UserResponse::fromEntity).toList();
    }

    public String deleteUser(Long userId) {
        UserEntity userEntity = repository.findByIdAndIsDeletedFalse(userId).orElse(null);
        if (userEntity == null) {
            throw new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString());
        }
        if (userEntity.getRole().equals(Role.ADMIN.name())) {
            throw new BadRequestException(ResponseMessage.USER_NOT_AUTHORIZED.name());
        }
        return softDelete(userId) ? "Account deleted successfully!" : "Account deleted failed!";
    }
}
