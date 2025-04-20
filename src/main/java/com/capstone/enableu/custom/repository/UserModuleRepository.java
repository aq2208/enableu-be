package com.capstone.enableu.custom.repository;

import com.capstone.enableu.common.repository.BaseRepository;
import com.capstone.enableu.custom.entity.UserModuleEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserModuleRepository extends BaseRepository<UserModuleEntity> {
    List<UserModuleEntity> findByModuleIdAndIsDeletedFalse(Long moduleId);

    List<UserModuleEntity> findByModuleIdAndUserIdIn(Long moduleId, List<Long> userIds);

    @Modifying
    @Transactional
    @Query("UPDATE UserModuleEntity u SET u.isDeleted = true WHERE u.module.id = :moduleId")
    void deletedByModuleId(@Param("moduleId") Long moduleId);
}
