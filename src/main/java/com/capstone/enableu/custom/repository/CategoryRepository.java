package com.capstone.enableu.custom.repository;

import com.capstone.enableu.common.repository.BaseRepository;
import com.capstone.enableu.custom.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends BaseRepository<CategoryEntity> {
    List<CategoryEntity> findByNameContainsIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalse(String searchString, String description);
    List<CategoryEntity> findByNameContainsIgnoreCaseAndIsDeletedFalse(String searchString);
    Page<CategoryEntity> findAllByIsDeletedFalse(Pageable pageable);
    Page<CategoryEntity> findByCreatedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c JOIN UserCategoryEntity u ON u.category.id = c.id WHERE u.user.id = :userId AND c.isDeleted = false AND u.isDeleted = false AND c.status ='ACTIVE' ")
    Page<CategoryEntity> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM CategoryEntity c WHERE c.createdByUserId = :userId AND c.isDeleted = false")
    Page<CategoryEntity> findAssignedOrCreatedCategories(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
                    select * from category where id not in (
                    
                        select id from category
                            where category.created_by_user_id = :userId
                            and category.is_deleted = false
                            and category.status = 'ACTIVE'
                    
                        UNION
                    
                        select category_id from user_category
                            where user_category.user_id = :userId
                            and user_category.is_deleted = false
                            and user_category.status = 'ACTIVE'
                    
                    ) and is_deleted = false and status = 'ACTIVE'
                    """, nativeQuery = true)
    Page<CategoryEntity> findListSuggestions(@Param("userId") Long userId, Pageable pageable);
}
