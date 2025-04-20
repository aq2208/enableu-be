package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.entity.TaskEntity;
import com.capstone.enableu.custom.entity.UserEntity;
import com.capstone.enableu.custom.entity.UserTaskEntity;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Status;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.UserTaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserTaskService extends BaseService<UserTaskEntity, UserTaskRepository> {
    private final UserService userService;

    public List<Long> findTraineeByTaskId(Long taskId) {
        return repository.findByTaskIdAndIsDeletedFalse(taskId).stream()
                .map(UserTaskEntity::getUser)
                .map(BaseEntity::getId)
                .toList();
    }

    public List<UserTaskEntity> findByTaskId(Long taskId) {
        return repository.findByTaskIdAndIsDeletedFalse(taskId);
    }

    public void createBulkWithListUserIds(TaskEntity taskEntity, List<Long> userIds) {
        userIds.forEach(id -> {
            UserTaskEntity userTaskEntity = new UserTaskEntity();
            userTaskEntity.setTask(taskEntity);
            UserEntity trainee = userService.findByIdAndNotDeleted(id);
            if (trainee == null) {
                throw new NotFoundException(ResponseMessage.USER_NOT_FOUND.toString());
            }
            userTaskEntity.setUser(trainee);
            userTaskEntity.setStatus(Status.ACTIVE.name());
            save(userTaskEntity);
        });
    }

    public List<UserTaskEntity> findByTaskIdAndUserIds(Long taskId, List<Long> userIds) {
        return repository.findByTaskIdAndUserIdInAndIsDeletedFalse(taskId, userIds);
    }

    public List<UserTaskEntity> findByUserIdAndIsDeletedFalse(Long userId) {
        return repository.findByUserIdAndStatusIsNotAndIsDeletedFalse(userId, Status.INACTIVE.name());
    }

}
