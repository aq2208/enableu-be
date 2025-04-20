package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.entity.UserCategoryEntity;
import com.capstone.enableu.custom.repository.UserCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserCategoryService extends BaseService<UserCategoryEntity, UserCategoryRepository> {
    public List<Long> findTraineeIdListByCategoryId(Long categoryId) {
        return repository.findTraineeIdListByCategoryId(categoryId);
    }

    //findTaskProcessIdByCategoryIdAndTraineeId
    public Long findTaskProcessIdByCategoryIdAndTraineeId(Long categoryId, Long traineeId) {
        return repository.findTaskProcessIdByCategoryIdAndUserIdAndIsDeletedFalse(categoryId, traineeId);
    }

    public UserCategoryEntity findByCategoryIdAndUserIdAndIsDeletedFalse(Long categoryId, Long traineeId) {
        return repository.findByCategoryIdAndUserIdAndIsDeletedFalse(categoryId, traineeId);
    }

    public List<UserCategoryEntity> findByUserIdAndIsDeletedFalse(Long categoryId) {
        return repository.findByUserIdAndIsDeletedFalse(categoryId);
    }

}
