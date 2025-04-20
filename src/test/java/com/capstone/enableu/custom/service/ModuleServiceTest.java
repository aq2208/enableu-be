package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.common.security.CurrentUserDetailContainer;
import com.capstone.enableu.common.security.CustomUserDetails;
import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.entity.ModuleEntity;
import com.capstone.enableu.custom.enums.Type;
import com.capstone.enableu.custom.repository.CategoryRepository;
import com.capstone.enableu.custom.repository.ModuleRepository;
import com.capstone.enableu.custom.request.ModuleRequest;
import com.capstone.enableu.custom.response.ModuleResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.capstone.enableu.custom.enums.Role.ADMIN;
import static com.capstone.enableu.custom.enums.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Transactional
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class ModuleServiceTest {

    private final Logger log = LoggerFactory.getLogger(ModuleServiceTest.class);

    @Value("${test.service.module.categoryId}")
    private Integer categoryId;

    @MockBean
    CurrentUserDetailContainer currentUserDetailContainer;
    CategoryRepository categoryRepository;

    @MockBean
    ModuleRepository moduleRepository;

    @InjectMocks
    ModuleService moduleService;

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
        categoryRepository = mock(CategoryRepository.class);
        ReflectionTestUtils.setField(moduleService, "categoryRepository", categoryRepository);
    }

    @Test
    void createModule() {
        setUpCreateModule();
        ModuleRequest request = getMockModuleRequest();
        ModuleResponse module = moduleService.createModule(categoryId, request);
        verifyCreateModule(request, module);
    }

    private void verifyCreateModule(ModuleRequest request, ModuleResponse module) {
        ModuleEntity moduleEntity = new ModuleEntity();
        assertNotNull(module.getId());
        assertNotNull(module.getCode());
        // ignore equality of ID & Code
        moduleEntity.setId(module.getId());
        moduleEntity.setCode(UUID.fromString(module.getCode()));
        verifyModuleDetail(module, request.toEntity(moduleEntity));
        verify(moduleRepository, times(1))
                .save(any(ModuleEntity.class));
    }

    private ModuleRequest getMockModuleRequest() {
        ModuleRequest request = new ModuleRequest();
        request.setType(Type.PUBLIC.name());
        request.setName("mock request name");
        request.setStatus(ACTIVE.name());
        return request;
    }

    private void setUpCreateModule() {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(Long.valueOf(categoryId));
        given(categoryRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(categoryEntity));
        given(moduleRepository.save(any(ModuleEntity.class)))
                .will(invocation -> {
                    ModuleEntity savedModule = invocation.getArgument(0);
                    savedModule.setId(1L);
                    return savedModule;
                });
    }

    private ModuleEntity getMockModuleEntity(Integer moduleId) {
        ModuleEntity expectedModule = new ModuleEntity();
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(Long.valueOf(categoryId));
        categoryEntity.setCode(UUID.randomUUID());
        expectedModule.setId(Long.valueOf(moduleId));
        expectedModule.setCode(UUID.randomUUID());
        expectedModule.setCategory(categoryEntity);
        expectedModule.setType(Type.PUBLIC.name());
        expectedModule.setName("mock name");
        expectedModule.setStatus(ACTIVE.name());
        return expectedModule;
    }

    @Test
    void softDeleteModule() {
        Integer moduleId = 1;
        ModuleEntity mockModuleEntity = getMockModuleEntity(moduleId);
        mockModuleEntity.setDeleted(false);
        given(moduleRepository.findById(anyLong()))
                .willReturn(Optional.of(mockModuleEntity));
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(mockModuleEntity));
        given(moduleRepository.save(any(ModuleEntity.class)))
                .will(invocation -> {
                    ModuleEntity savedModule = invocation.getArgument(0);
                    assertTrue(savedModule.isDeleted());
                    return savedModule;
                });
        moduleService.softDeleteModule(moduleId);
        verify(moduleRepository, times(1)).save(any(ModuleEntity.class));
    }

    @Test
    void softDeleteModuleWithAlreadyDeletedValue() {
        Integer moduleId = 1;
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        ModuleEntity softDeletedEntity = getMockModuleEntity(moduleId);
        softDeletedEntity.setDeleted(true);
        given(moduleRepository.findById(anyLong()))
                .willReturn(Optional.of(softDeletedEntity));
        given(moduleRepository.save(any(ModuleEntity.class)))
                .will(invocation -> {
                    ModuleEntity savedModule = invocation.getArgument(0);
                    assertTrue(savedModule.isDeleted());
                    return savedModule;
                });
        assertThrows(EntityNotFoundException.class
                , () -> moduleService.softDeleteModule(moduleId));
    }

    @Test
    void updateModule() {
        Integer moduleId = 1;
        ModuleRequest mockUpdatedModuleRequest = getMockModuleRequest();
        ModuleEntity mockModuleEntity = getMockModuleEntity(moduleId);
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(mockModuleEntity));
        given(moduleRepository.findById(anyLong()))
                .willReturn(Optional.of(mockModuleEntity));
        given(moduleRepository.save(any(ModuleEntity.class)))
                .will(invocation -> invocation.<ModuleEntity>getArgument(0));
        ModuleResponse moduleResponse = moduleService.updateModule(moduleId
                , mockUpdatedModuleRequest);
        ModuleEntity originValue = getMockModuleEntity(1);
        verify(moduleRepository, times(1)).save(any(ModuleEntity.class));
        verifyModuleDetailUpdated(moduleResponse, originValue);
        assertNotNull(moduleResponse.getId());
        assertNotNull(moduleResponse.getCode());
        originValue.setCode(UUID.fromString(moduleResponse.getCode()));
        verifyModuleDetail(moduleResponse
                , mockUpdatedModuleRequest.toEntity(originValue));
    }

    private void verifyModuleDetailUpdated(ModuleResponse moduleDetail, ModuleEntity expectedModule) {
        boolean atLeastOneFieldDifferent = !Objects.equals(expectedModule.getId(), moduleDetail.getId());

        if (!Objects.equals(expectedModule.getName(), moduleDetail.getName())) {
            atLeastOneFieldDifferent = true;
        }
        if (!Objects.equals(expectedModule.getStatus(), moduleDetail.getStatus())) {
            atLeastOneFieldDifferent = true;
        }
        if (Objects.isNull(expectedModule.getCode())) {
            if (Objects.nonNull(moduleDetail.getCode())) {
                atLeastOneFieldDifferent = true;
            }
        } else if (!Objects.equals(expectedModule.getCode().toString(), moduleDetail.getCode())) {
            atLeastOneFieldDifferent = true;
        }

        if (!atLeastOneFieldDifferent) {
            fail("No fields were updated; expected at least one field to be different.");
        }
    }

    @Test
    void updateModuleWithSoftDeletedValue() {
        Integer moduleId = 1;
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        ModuleEntity softDeletedEntity = getMockModuleEntity(moduleId);
        softDeletedEntity.setDeleted(true);
        given(moduleRepository.findById(anyLong()))
                .willReturn(Optional.of(softDeletedEntity));
        given(moduleRepository.save(any(ModuleEntity.class)))
                .will(invocation -> invocation.<ModuleEntity>getArgument(0));
        assertThrows(EntityNotFoundException.class
                , () -> moduleService.updateModule(moduleId
                        , getMockModuleRequest()));
    }

    @Test
    void getModuleDetail() {
        Integer moduleId = 1;
        ModuleEntity expectedModule = setUpModuleDetail(moduleId);
        ModuleResponse moduleDetail = moduleService.getModuleDetail(moduleId);
        verifyModuleDetail(moduleDetail, expectedModule);
    }

    @Test
    void getModuleDetailWithSoftDeletedValue() {
        Integer moduleId = 1;
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        ModuleEntity softDeletedModule = getMockModuleEntity(moduleId);
        softDeletedModule.setDeleted(true);
        given(moduleRepository.findById(anyLong()))
                .willReturn(Optional.of(softDeletedModule));
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class
                , () -> moduleService.getModuleDetail(moduleId));
    }

    private void verifyModuleDetail(ModuleResponse moduleDetail, ModuleEntity expectedModule) {
        assertEquals(expectedModule.getId(), moduleDetail.getId());
        assertEquals(expectedModule.getType(), moduleDetail.getType());
        assertEquals(expectedModule.getName(), moduleDetail.getName());
        assertEquals(expectedModule.getStatus(), moduleDetail.getStatus());
        if (Objects.isNull(expectedModule.getTasks())) {
            assertNull(moduleDetail.getTasks());
        } else {
            assertEquals(expectedModule.getTasks().size(), moduleDetail.getTasks().size());
        }
        if (Objects.isNull(expectedModule.getCode())) {
            assertNull(moduleDetail.getCode());
        }
        assertEquals(expectedModule.getCode().toString(), moduleDetail.getCode());
    }

    private ModuleEntity setUpModuleDetail(Integer moduleId) {
        ModuleEntity expectedModule = getMockModuleEntity(moduleId);

        given(moduleRepository.findById(anyLong()))
                .willReturn(Optional.of(expectedModule));
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.of(expectedModule));
        return expectedModule;
    }

    @Test
    void getModuleList() {
        Page<ModuleEntity> preparedModulePage = setupModuleList();
        List<ModuleResponse> resultModuleList = moduleService
                .getModuleListOfCategory(categoryId, 1, 10).getContent();
        verifyModuleList(resultModuleList, preparedModulePage);
    }

    @Test
    void getModuleListWithSoftDeletedValues() {
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        ModuleEntity softDeletedModule = getMockModuleEntity(1);
        softDeletedModule.setDeleted(true);
        given(moduleRepository.findByCategoryIdAndIsDeletedFalse(anyLong(), any(Pageable.class)))
                .willReturn(Page.empty());
        given(moduleRepository.findByIdAndIsDeletedFalse(anyLong()))
                .willReturn(Optional.empty());
        List<ModuleResponse> result = moduleService
                .getModuleListOfCategory(categoryId, 1, 10).getContent();
        assertEquals(0, result.size());

    }
    private void verifyModuleList(
            List<ModuleResponse> resultModuleList
            , Page<ModuleEntity> preparedModulePage) {
        assertThat(resultModuleList).isNotNull();
        assertThat(resultModuleList.size())
                .isEqualTo(preparedModulePage.stream().toList().size());
        for (ModuleResponse moduleResponse : resultModuleList) {
            ModuleEntity expectedModule = preparedModulePage.stream()
                    .filter(moduleEntity -> Optional.of(moduleEntity)
                            .map(BaseEntity::getCode)
                            .map(UUID::toString)
                            .map(code -> code.equals(moduleResponse.getCode()))
                            .orElse(Boolean.FALSE)
                    )
                    .findFirst()
                    .orElse(null);
            assertNotNull(expectedModule);
            verifyModuleDetail(moduleResponse, expectedModule);
        }
    }

    private Page<ModuleEntity> setupModuleList() {
        int page = 0;
        int size = 10;
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setId(Long.valueOf(categoryId));
        List<ModuleEntity> moduleList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ModuleEntity moduleEntity = new ModuleEntity();
            moduleEntity.setCategory(categoryEntity);
            moduleEntity.setCode(UUID.randomUUID());
            moduleEntity.setName("Module" + i);
            moduleList.add(moduleEntity);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<ModuleEntity> expectedModulePage = new PageImpl<>(moduleList
                , pageable
                , moduleList.size());
        given(moduleRepository.findByCategoryIdAndIsDeletedFalse(anyLong(), any(Pageable.class)))
                .willReturn(expectedModulePage);
        return expectedModulePage;
    }

    @AfterEach
    public void tearDown() throws Exception {
        autoCloseable.close();
    }
}