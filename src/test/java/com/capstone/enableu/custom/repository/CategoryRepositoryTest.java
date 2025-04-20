package com.capstone.enableu.custom.repository;

import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.enums.Status;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CategoryRepositoryTest {
    private static final Logger log = LoggerFactory.getLogger(CategoryRepositoryTest.class);
    @Autowired CategoryRepository categoryRepository;

    @Test
    void test() {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName("test");
        categoryEntity.setCode(UUID.randomUUID());
        categoryEntity.setCreatedTime(new Date());
        categoryEntity.setUpdatedTime(new Date());
        categoryEntity.setStatus(Status.ACTIVE.name());
        categoryEntity.setDeleted(true);
        CategoryEntity saveCategory = categoryRepository.save(categoryEntity);
        log.info("{}", saveCategory);

        Page<CategoryEntity> result = categoryRepository.findAllByIsDeletedFalse(Pageable.ofSize(10));
        assertEquals(0, result.stream().toList().size());

    }
}