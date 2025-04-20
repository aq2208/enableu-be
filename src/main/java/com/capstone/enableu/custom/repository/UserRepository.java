package com.capstone.enableu.custom.repository;

import com.capstone.enableu.common.repository.BaseRepository;
import com.capstone.enableu.custom.entity.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends BaseRepository<UserEntity> {

    List<UserEntity> findByFullNameIsContainingIgnoreCase(String fullName);
    Optional<UserEntity> findByUsernameAndIsDeletedFalse(String username);
    Optional<UserEntity> findByUsernameAndIsDeletedFalseAndStatusNotContaining(String username, String status);
    Optional<UserEntity> findByIdAndIsDeletedFalse(Long id);
    List<UserEntity> findByIsDeletedFalseAndFullNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseAndIsDeletedFalse(String fullName, String username);

    Optional<UserEntity> findByPhone(String phone);

    @Query(value = """
            SELECT *
            FROM user u
            WHERE 1=1 
            AND u.id <> :currentUserId
            AND u.status = 'ACTIVE'
            AND u.role in ('TRAINEE')
            AND u.is_deleted = 0
            """, nativeQuery = true)
    List<UserEntity> findAllActiveTrainee(Long currentUserId);

    @Query(value = """
            SELECT *
            FROM user u
            WHERE 1=1 
            AND u.id in (
                SELECT uc.user_id FROM user_category uc 
                WHERE 1=1 
                AND uc.category_id = :categoryId
                AND uc.is_deleted = 0                                        
            )
            AND u.id <> :currentUserId
            AND u.status = 'ACTIVE'
            AND u.role in ('TRAINEE')
            AND u.is_deleted = 0
            """, nativeQuery = true)
    List<UserEntity> findAllActiveTraineeOfCategory(Long categoryId, Long currentUserId);

    @Query(value = """
            SELECT *
            FROM user u
            WHERE 1=1 
            AND u.id in (
                SELECT ut.user_id FROM user_task ut
                WHERE 1=1 
                AND ut.task_id = :taskId
                AND ut.is_deleted = 0
            )
            AND u.id <> :currentUserId
            AND u.status = 'ACTIVE'
            AND u.role in ('TRAINEE')
            AND u.is_deleted = 0
            """, nativeQuery = true)
    List<UserEntity> findAllActiveTraineeOfTask(Long taskId, Long currentUserId);
}