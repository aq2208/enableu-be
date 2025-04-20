package com.capstone.enableu.custom.repository;

import com.capstone.enableu.common.repository.BaseRepository;
import com.capstone.enableu.custom.entity.ModuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends BaseRepository<ModuleEntity> {
    Page<ModuleEntity> findByCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);
}
