package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.entity.ModuleEntity;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.entity.UserModuleEntity;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Status;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.UserModuleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserModuleService extends BaseService<UserModuleEntity, UserModuleRepository> {
    private final UserService userService;
    public List<Long> findTraineeByModuleId(Long moduleId) {
        return repository.findByModuleIdAndIsDeletedFalse(moduleId).stream()
                .map(UserModuleEntity::getUser)
                .map(BaseEntity::getId)
                .toList();
    }

    public void createBulkWithListUserIds(ModuleEntity moduleEntity, List<Long> userIds) {
        for (Long traineeId : userIds) {
            UserModuleEntity userModuleEntity = new UserModuleEntity();
            userModuleEntity.setModule(moduleEntity);
            UserEntity trainee = userService.findByIdAndNotDeleted(traineeId);
            if (trainee == null) {
                throw new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString());
            }
            userModuleEntity.setUser(trainee);
            userModuleEntity.setStatus(Status.ACTIVE.name());
            save(userModuleEntity);
        }
    }

    public List<UserModuleEntity> findByModuleIdAndUserId(Long moduleId, List<Long> userIds) {
        return repository.findByModuleIdAndUserIdIn(moduleId, userIds);
    }

    public void deleteUserModuleByModuleId(Long moduleId) {
        repository.deletedByModuleId(moduleId);
    }
}
