package com.capstone.enableu.custom.repository;

import com.capstone.enableu.common.repository.BaseRepository;
import com.capstone.enableu.custom.entity.TaskEntity;
import com.capstone.enableu.custom.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends BaseRepository<TaskEntity> {
    List<TaskEntity> findByNameContainsIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalse(
            String name, String description);

    List<TaskEntity> findByNameContainsIgnoreCaseAndModuleNullAndTypeContainingAndIsDeletedFalse(String name,
            String type);

    List<TaskEntity> findByCreatedByUserIdAndIsReviewModeTrueAndIsDeletedFalse(Long userId);

    Page<TaskEntity> findByModuleIdAndIsDeletedFalse(Long moduleId, Pageable pageable);

    @Query(value = """
            SELECT t.* FROM task t JOIN user_task u ON u.task_id = t.id WHERE (t.created_by_user_id = :userId OR u.user_id = :userId) AND u.is_deleted = 0 AND t.is_deleted = 0 AND t.type = :type AND t.module_id IS NULL ORDER BY ?#{#pageable}
            """, countQuery = """
            SELECT COUNT(*) FROM task t JOIN user_task u ON u.task_id = t.id WHERE (t.created_by_user_id = :userId OR u.user_id = :userId) AND u.is_deleted = 0 AND t.is_deleted = 0 AND t.type = :type AND t.module_id IS NULL
            """, nativeQuery = true)
    Page<TaskEntity> findByCreatedByUserIdAndTypeContainingAndIsDeletedFalse(Long userId, String type,
            Pageable pageable);

    @Query(value = """
            SELECT MAX(t.order_id) FROM task t WHERE t.module_id = :moduleId
            """, nativeQuery = true)
    Integer findMaxOrderIdByModuleId(Long moduleId);
}
