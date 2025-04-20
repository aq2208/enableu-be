package com.capstone.enableu.custom.repository;

import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.entity.ModuleEntity;
import com.capstone.enableu.custom.enums.Status;
import com.capstone.enableu.custom.enums.Type;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ModuleRepositoryTest {
    private static final Logger log = LoggerFactory.getLogger(ModuleRepositoryTest.class);
    @Autowired ModuleRepository moduleRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    void test() {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("test");
        categoryEntity.setCode(UUID.randomUUID());
        categoryEntity.setCreatedTime(new Date());
        categoryEntity.setUpdatedTime(new Date());
        categoryEntity.setStatus(Status.ACTIVE.name());
        CategoryEntity saveCategory = categoryRepository.save(categoryEntity);
        log.info("{}", saveCategory);

        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity.setCode(UUID.randomUUID());
        moduleEntity.setCreatedTime(new Date());
        moduleEntity.setUpdatedTime(new Date());
        moduleEntity.setName("test");
        moduleEntity.setDeleted(true);
        moduleEntity.setCategory(saveCategory);
        moduleEntity.setOrderId(0);
        moduleEntity.setStatus(Status.ACTIVE.name());
        moduleEntity.setType(Type.PUBLIC.name());

        moduleRepository.save(moduleEntity);
        Page<ModuleEntity> result = moduleRepository.findByCategoryIdAndIsDeletedFalse(saveCategory.getId(), Pageable.ofSize(10));
        assertEquals(0, result.stream().toList().size());

    }

}