package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.common.resolver.BaseResolver;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.request.ChangePasswordRequest;
import com.capstone.enableu.custom.request.CreateUserRequest;
import com.capstone.enableu.custom.dto.UserFilter;
import com.capstone.enableu.custom.response.PaginationResponse;
import com.capstone.enableu.custom.response.UserResponse;
import com.capstone.enableu.custom.request.UpdateUserProfile;
import com.capstone.enableu.custom.dto.ShortcutInfo;
import com.capstone.enableu.custom.service.SmsService;
import com.capstone.enableu.custom.service.UserService;
import lombok.AllArgsConstructor;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;


@Controller
@AllArgsConstructor
public class UserResolver extends BaseResolver<UserService>{

    private final SmsService smsService;

    // sendSMS
    @MutationMapping
//    @Secured(Role.TRAINEE_CONSTANT)
    public String sendSMS(@Argument String to, @Argument String code) throws IOException {
        return "hahaha";
    }


    @QueryMapping
//    @Secured(Role.TRAINEE_CONSTANT)
    public String checkHealth() {
        return service.ok();
    }

    @MutationMapping(name = "register")
    public UserEntity registerUser(@Argument(name = "input") CreateUserRequest createUserRequest) {
        return service.register(createUserRequest);
    }

    @MutationMapping
    @Secured(Role.TRAINEE_CONSTANT)
    public UserEntity changePassword(@Argument(name = "input") ChangePasswordRequest request) {
        return service.changePassword(request);
    }

    @MutationMapping
    public String forgotPassword(@Argument(name = "username") String username) throws IOException {
        return service.forgotPassword(username);
    }

    @MutationMapping
    @Secured(Role.ADMIN_CONSTANT)
    public String deleteAccount(@Argument(name = "id") Long id) {
        return service.deleteUser(id);
    }

    // update status
    @MutationMapping
    @Secured(Role.ADMIN_CONSTANT)
    public UserEntity updateUserStatus(@Argument(name = "id") Long id, @Argument(name = "status") String status) {
        return service.updateStatus(id, status);
    }

    @QueryMapping(name = "getAllActiveTrainee")
    @Secured(Role.TRAINER_CONSTANT)
    public List<UserResponse> getAllActiveTrainee() {
        return service.getAllActiveTrainee();
    }

    @QueryMapping(name = "getAllActiveTraineeOfCategory")
    @Secured(Role.TRAINER_CONSTANT)
    public List<UserResponse> getAllActiveTraineeOfCategory(@Argument(name = "categoryId") Long categoryId) {
        return service.getAllActiveTraineeOfCategory(categoryId);
    }

    @QueryMapping(name = "getAllActiveTraineeOfTask")
    @Secured(Role.TRAINER_CONSTANT)
    public List<UserResponse> getAllActiveTraineeOfTask(@Argument(name = "taskId") Long taskId) {
        return service.getAllActiveTraineeOfTask(taskId);
    }

    @MutationMapping(name = "updateUserProfile")
    @Secured(Role.TRAINEE_CONSTANT)
    public UserResponse updateUserProfile(@Argument(name = "id") Long id, @Argument(name = "input") UpdateUserProfile updateUserProfile) {
        return service.updateUserProfile(id, updateUserProfile);
    }

    @QueryMapping(name = "getUserById")
    @Secured(Role.TRAINEE_CONSTANT)
    public UserResponse getUserById(@Argument(name = "id") Long id) {
        return service.getUserDetail(id);
    }

    @MutationMapping(name="updateUserAvatar")
    @Secured(Role.TRAINEE_CONSTANT)
    public String updateUserAvatar(@Argument(name = "id") Long id, @Argument(name = "avatar") String avatar) {
        return service.updateUserAvatar(id, avatar);
    }

    @MutationMapping(name="updateShortcut")
    @Secured(Role.TRAINEE_CONSTANT)
    public UserResponse updateShortcut(@Argument(name = "id") Long id, @Argument(name = "input") List<ShortcutInfo> shortcutInfo) {
        return service.updateShortcut(id, shortcutInfo);
    }

    @QueryMapping(name = "getUserList")
    public PaginationResponse<UserResponse> getUserList(
            @Argument(name = "filter") UserFilter filter
            , @Argument(name = "keyword") String keyword
            , @Argument(name = "pageNumber") Integer pageNumber
            , @Argument(name = "pageSize") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return PaginationResponse.of(service.getUserList(filter, keyword), pageable);
    }
}