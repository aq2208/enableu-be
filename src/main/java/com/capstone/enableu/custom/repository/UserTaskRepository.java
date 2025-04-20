package com.capstone.enableu.custom.repository;

import com.capstone.enableu.common.repository.BaseRepository;
import com.capstone.enableu.custom.entity.UserTaskEntity;

import java.util.List;

public interface UserTaskRepository extends BaseRepository<UserTaskEntity> {
    List<UserTaskEntity> findByTaskIdAndIsDeletedFalse(Long task_id);
    List<UserTaskEntity> findByTaskIdAndUserIdInAndIsDeletedFalse(Long taskId, List<Long> userIds);
    List<UserTaskEntity> findByUserIdAndStatusIsNotAndIsDeletedFalse(Long userId, String status);
}
