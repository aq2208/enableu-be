package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.common.security.CurrentUserDetailContainer;
import com.capstone.enableu.common.security.CustomUserDetails;
import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.repository.CategoryRepository;
import com.capstone.enableu.custom.request.CategoryRequest;
import com.capstone.enableu.custom.response.CategoryResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.capstone.enableu.custom.enums.Role.ADMIN;
import static com.capstone.enableu.custom.enums.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Transactional
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class CategoryServiceTest {
    private final Logger log = LoggerFactory.getLogger(CategoryServiceTest.class);

    @MockBean
    CurrentUserDetailContainer currentUserDetailContainer;

    @MockBean
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void setup() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        given(currentUserDetailContainer.getCurrentUser())
                .willReturn(new CustomUserDetails(
                        1L,
                        "sampleUsername",
                        "samplePassword",
                        List.of(new SimpleGrantedAuthority(ADMIN.name()))
                ));
    }

    @Test
    void createCategory() {
        setUpCreateCategory();
        CategoryRequest request = getMockCategoryRequest();
        CategoryResponse category = categoryService.createCategory(request);
        verifyCreateCategory(request, category);
    }

    private void verifyCreateCategory(CategoryRequest request, CategoryResponse category) {
        CategoryEntity categoryEntity = new CategoryEntity();
        assertNotNull(category.getId());
        assertNotNull(category.getCode());
        // ignore equality of ID & Code
        categoryEntity.setId(category.getId());
        categoryEntity.setCode(UUID.fromString(category.getCode()));
        verifyCategoryDetail(category, request.toEntity(categoryEntity));
        verify(categoryRepository, times(1))
                .save(any(CategoryEntity.class));
    }

    private CategoryRequest getMockCategoryRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setAttachment("mock request attachment");
        request.setThumbnail("mock request thumbnail");
        request.setName("mock request name");
        request.setDescription("mock request description");
        request.setStatus(ACTIVE.name());
        return request;
    }

    private void setUpCreateCategory() {
        given(categoryRepository.save(any(CategoryEntity.class)))
                .will(invocation -> {
                    CategoryEntity savedCategory = invocation.getArgument(0);
                    savedCategory.setId(1L);
                    return savedCategory;
                });
    }

    private static CategoryEntity getMockCategoryEntity(Integer categoryId) {
        CategoryEntity expectedCategory = new CategoryEntity();
        expectedCategory.setId(Long.valueOf(categoryId));
        expectedCategory.setCode(UUID.randomUUID());
        expectedCategory.setAttachment("mock attachment");
        expectedCategory.setThumbnail("mock thumbnail");
        expectedCategory.setName("mock name");
        expectedCategory.setDescription("mock description");
        expectedCategory.setStatus(ACTIVE.name());
        return expectedCategory;
    }

    @Test
    void softDeleteCategory() {
        Integer categoryId = 1;
        CategoryEntity mockCategoryEntity = getMockCategoryEntity(categoryId);
        mockCategoryEntity.setDeleted(false);
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(mockCategoryEntity));
        given(categoryRepository.save(any(CategoryEntity.class)))
                .will(invocation -> {
                    CategoryEntity savedCategory = invocation.getArgument(0);
                    assertTrue(savedCategory.isDeleted());
                    return savedCategory;
                });
        categoryService.softDeleteCategory(categoryId);
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void softDeleteCategoryWithAlreadyDeletedValue() {
        Integer categoryId = 1;
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        CategoryEntity softDeletedEntity = getMockCategoryEntity(categoryId);
        softDeletedEntity.setDeleted(true);
        given(categoryRepository.findById(anyLong()))
                .willReturn(Optional.of(softDeletedEntity));
        given(categoryRepository.save(any(CategoryEntity.class)))
                .will(invocation -> {
                    CategoryEntity savedCategory = invocation.getArgument(0);
                    assertTrue(savedCategory.isDeleted());
                    return savedCategory;
                });
        assertThrows(EntityNotFoundException.class
                , () -> categoryService.softDeleteCategory(categoryId));
    }

    @Test
    void updateCategory() {
        Integer categoryId = 1;
        CategoryRequest mockUpdatedCategoryRequest = getMockCategoryRequest();
        CategoryEntity mockCategoryEntity = getMockCategoryEntity(categoryId);
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(mockCategoryEntity));
        given(categoryRepository.findById(anyLong()))
                .willReturn(Optional.of(mockCategoryEntity));
        given(categoryRepository.save(any(CategoryEntity.class)))
                .will(invocation -> invocation.<CategoryEntity>getArgument(0));
        CategoryResponse categoryResponse = categoryService.updateCategory(categoryId
                , mockUpdatedCategoryRequest);
        CategoryEntity originValue = getMockCategoryEntity(1);
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
        verifyCategoryDetailUpdated(categoryResponse, originValue);
        assertNotNull(categoryResponse.getId());
        assertNotNull(categoryResponse.getCode());
        originValue.setCode(UUID.fromString(categoryResponse.getCode()));
        verifyCategoryDetail(categoryResponse
                , mockUpdatedCategoryRequest.toEntity(originValue));
    }

    private void verifyCategoryDetailUpdated(CategoryResponse categoryDetail, CategoryEntity expectedCategory) {
        boolean atLeastOneFieldDifferent = false;

        if (!Objects.equals(expectedCategory.getId(), categoryDetail.getId())) {
            atLeastOneFieldDifferent = true;
        }
        if (!Objects.equals(expectedCategory.getAttachment(), categoryDetail.getAttachment())) {
            atLeastOneFieldDifferent = true;
        }
        if (!Objects.equals(expectedCategory.getThumbnail(), categoryDetail.getThumbnail())) {
            atLeastOneFieldDifferent = true;
        }
        if (!Objects.equals(expectedCategory.getName(), categoryDetail.getName())) {
            atLeastOneFieldDifferent = true;
        }
        if (!Objects.equals(expectedCategory.getStatus(), categoryDetail.getStatus())) {
            atLeastOneFieldDifferent = true;
        }
        if (!Objects.equals(expectedCategory.getDescription(), categoryDetail.getDescription())) {
            atLeastOneFieldDifferent = true;
        }
        if (Objects.isNull(expectedCategory.getCode())) {
            if (Objects.nonNull(categoryDetail.getCode())) {
                atLeastOneFieldDifferent = true;
            }
        } else if (!Objects.equals(expectedCategory.getCode().toString(), categoryDetail.getCode())) {
            atLeastOneFieldDifferent = true;
        }

        if (!atLeastOneFieldDifferent) {
            fail("No fields were updated; expected at least one field to be different.");
        }
    }

    @Test
    void updateCategoryWithSoftDeletedValue() {
        Integer categoryId = 1;
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        CategoryEntity softDeletedEntity = getMockCategoryEntity(categoryId);
        softDeletedEntity.setDeleted(true);
        given(categoryRepository.findById(anyLong()))
                .willReturn(Optional.of(softDeletedEntity));
        given(categoryRepository.save(any(CategoryEntity.class)))
                .will(invocation -> invocation.<CategoryEntity>getArgument(0));
        assertThrows(EntityNotFoundException.class
                , () -> categoryService.updateCategory(categoryId
                        , getMockCategoryRequest()));
    }

    @Test
    void getCategoryDetail() {
        Integer categoryId = 1;
        CategoryEntity expectedCategory = setUpCategoryDetail(categoryId);
        CategoryResponse categoryDetail = categoryService.getCategoryDetail(categoryId);
        verifyCategoryDetail(categoryDetail, expectedCategory);
    }

    @Test
    void getCategoryDetailWithSoftDeletedValue() {
        Integer categoryId = 1;
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        CategoryEntity softDeletedCategory = getMockCategoryEntity(categoryId);
        softDeletedCategory.setDeleted(true);
        given(categoryRepository.findById(anyLong()))
                .willReturn(Optional.of(softDeletedCategory));
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class
                , () -> categoryService.getCategoryDetail(categoryId));
    }

    private void verifyCategoryDetail(CategoryResponse categoryDetail, CategoryEntity expectedCategory) {
        assertEquals(expectedCategory.getId(), categoryDetail.getId());
        assertEquals(expectedCategory.getAttachment(), categoryDetail.getAttachment());
        assertEquals(expectedCategory.getThumbnail(), categoryDetail.getThumbnail());
        assertEquals(expectedCategory.getName(), categoryDetail.getName());
        assertEquals(expectedCategory.getStatus(), categoryDetail.getStatus());
        assertEquals(expectedCategory.getDescription(), categoryDetail.getDescription());
        if (Objects.isNull(expectedCategory.getCode())) {
            assertNull(categoryDetail.getCode());
        }
        assertEquals(expectedCategory.getCode().toString(), categoryDetail.getCode());
    }

    private CategoryEntity setUpCategoryDetail(Integer categoryId) {
        CategoryEntity expectedCategory = getMockCategoryEntity(categoryId);

        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(expectedCategory));
        return expectedCategory;
    }

    @Test
    void getCategoryList() {
        Page<CategoryEntity> preparedCategoryPage = setupCategoryList();
        List<CategoryResponse> resultCategoryList = categoryService.getCategoryList(1, 10).getContent();
        verifyCategoryList(resultCategoryList, preparedCategoryPage);
    }

    @Test
    void getCategoryListWithSoftDeletedValues() {
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        CategoryEntity softDeletedCategory = getMockCategoryEntity(1);
        softDeletedCategory.setDeleted(true);
        given(categoryRepository.findAllByIsDeletedFalse(any(Pageable.class)))
                .willReturn(Page.empty());
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        List<CategoryResponse> result = categoryService.getCategoryList(1, 10).getContent();
        assertEquals(0, result.size());

    }
    private void verifyCategoryList(
            List<CategoryResponse> resultCategoryList
            , Page<CategoryEntity> preparedCategoryPage) {
        assertThat(resultCategoryList).isNotNull();
        assertThat(resultCategoryList.size())
                .isEqualTo(preparedCategoryPage.stream().toList().size());
        for (CategoryResponse categoryResponse : resultCategoryList) {
            CategoryEntity expectedCategory = preparedCategoryPage.stream()
                    .filter(categoryEntity -> Optional.of(categoryEntity)
                            .map(BaseEntity::getCode)
                            .map(UUID::toString)
                            .map(code -> code.equals(categoryResponse.getCode()))
                            .orElse(Boolean.FALSE)
                    )
                    .findFirst()
                    .orElse(null);
            assertNotNull(expectedCategory);
            verifyCategoryDetail(categoryResponse, expectedCategory);
        }
    }

    private Page<CategoryEntity> setupCategoryList() {
        int page = 0;
        int size = 10;
        List<CategoryEntity> categoryList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setCode(UUID.randomUUID());
            categoryEntity.setName("Category" + i);
            categoryList.add(categoryEntity);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryEntity> expectedCategoryPage = new PageImpl<>(categoryList
                , pageable
                , categoryList.size());
        given(categoryRepository.findAllByIsDeletedFalse(any(Pageable.class)))
                .willReturn(expectedCategoryPage);
        return expectedCategoryPage;
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }
}