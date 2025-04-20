package com.capstone.enableu.custom.repository;

import com.capstone.enableu.common.repository.BaseRepository;
import com.capstone.enableu.custom.entity.UserCategoryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCategoryRepository extends BaseRepository<UserCategoryEntity> {
    @Query(value = """
            select user_id from user_category
            where category_id = :categoryId and is_deleted = 0
            """, nativeQuery = true)
    List<Long> findTraineeIdListByCategoryId(Long categoryId);

    //findTaskProcessIdByCategoryIdAndTraineeId only 1
    @Query(value = """
            select task_process_id from user_category
            where category_id = :categoryId and user_id = :traineeId and is_deleted = 0
            """, nativeQuery = true)
    Long findTaskProcessIdByCategoryIdAndUserIdAndIsDeletedFalse(Long categoryId, Long traineeId);

    @Query(value = """
            select * from user_category
            where category_id = :categoryId and user_id = :traineeId and is_deleted = 0
            """, nativeQuery = true)
    UserCategoryEntity findByCategoryIdAndUserIdAndIsDeletedFalse(Long categoryId, Long traineeId);

    List<UserCategoryEntity> findByUserIdAndIsDeletedFalse(Long userId);
}
