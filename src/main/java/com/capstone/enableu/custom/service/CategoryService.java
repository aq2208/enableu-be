package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.dto.CategoryFilter;
import com.capstone.enableu.custom.entity.*;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.enums.Status;
import com.capstone.enableu.custom.enums.Type;
import com.capstone.enableu.custom.exception.BadRequestException;
import com.capstone.enableu.custom.exception.InvalidTokenException;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.CategoryRepository;
import com.capstone.enableu.custom.request.CategoryRequest;
import com.capstone.enableu.custom.request.OrderRequest;
import com.capstone.enableu.custom.response.*;
import com.capstone.enableu.custom.util.ListCompare;
import jakarta.transaction.Transactional;
import liquibase.logging.LogService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class CategoryService extends BaseService<CategoryEntity, CategoryRepository> {

    private final ModuleService moduleService;
    private final TaskService taskService;
    private final UserCategoryService userCategoryService;
    private final UserService userService;
    private final UserModuleService userModuleService;
    private final UserTaskService userTaskService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    @Transactional
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity = categoryRequest.toEntity(categoryEntity);
        CategoryEntity newCategory = save(categoryEntity);
        if (categoryRequest.getTraineeIdList() == null
                || categoryRequest.getTraineeIdList().isEmpty()
                || categoryRequest.getTraineeIdList().size() == 0) {
            return CategoryResponse.fromEntity(newCategory);
        }
        ListCompare.convertIntegerToLongList(categoryRequest.getTraineeIdList()).forEach(id -> {
            UserCategoryEntity userCategory = new UserCategoryEntity();
            userCategory.setCategory(newCategory);
            UserEntity trainee = userService.findByIdAndNotDeleted(id);
            if (trainee == null) {
                throw new NotFoundException(ResponseMessage.USER_NOT_FOUND.name());
            }
            userCategory.setUser(trainee);
            userCategory.setStatus(Status.ACTIVE.name());
            userCategoryService.save(userCategory);
        });
        CategoryResponse categoryResponse = CategoryResponse.fromEntity(newCategory);
        categoryResponse.setTraineeIdList(userCategoryService.findTraineeIdListByCategoryId(newCategory.getId()));
        return categoryResponse;
    }

    @Transactional
    public String softDeleteCategory(Integer id) {
        Long categoryId = (long) id;
        List<ModuleEntity> moduleList = findByIdAndNotDeleted(categoryId).getModules();
        if (moduleList != null) {
            for (ModuleEntity module : moduleList) {
                moduleService.softDeleteModule(module.getId().intValue());
            }
        }
        boolean deleted = softDelete(categoryId);
        if (!deleted) {
            throw new BadRequestException(ResponseMessage.CATEGORY_DELETED_FAILED.name());
        }
        return ResponseMessage.CATEGORY_DELETED_SUCCESSFULLY.name();
    }

    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest categoryRequest) {
        Long categoryId = (long) id;
        log.info(id.toString());
        log.info(categoryRequest.getName());
        log.info(categoryRequest.getDescription());
        log.info(categoryRequest.getStatus());
        CategoryEntity categoryEntity = findByIdAndNotDeleted(categoryId);
        if (categoryEntity == null) {
            log.info(ResponseMessage.CATEGORY_NOT_FOUND.name());
            throw new NotFoundException(ResponseMessage.CATEGORY_NOT_FOUND.name());
        }
        CategoryEntity finalCategoryEntity = categoryEntity;
        ListCompareResponse listCompareResponse = ListCompare.compareLists(
                userCategoryService.findTraineeIdListByCategoryId(categoryId),
                ListCompare.convertIntegerToLongList(categoryRequest.getTraineeIdList())
        );

        // add new items
        listCompareResponse.newItemList().forEach(traineeId -> {
            UserCategoryEntity userCategory = new UserCategoryEntity();
            userCategory.setCategory(finalCategoryEntity);
            UserEntity trainee = userService.findByIdAndNotDeleted(traineeId);
            if (trainee == null) {
                log.info(ResponseMessage.USER_NOT_FOUND.name());
                throw new NotFoundException(ResponseMessage.USER_NOT_FOUND.name());
            }
            userCategory.setUser(trainee);
            userCategory.setStatus(Status.ACTIVE.name());
            userCategoryService.save(userCategory);
        });
        List<Long> traineeIdList = TraineeIdListWithModules(categoryEntity);
        if (new HashSet<>(traineeIdList).stream().anyMatch(listCompareResponse.oldItemList()::contains)) {
            throw new BadRequestException(ResponseMessage.TRAINEE_ID_IS_USED.name());
        }

        // delete removed items
        listCompareResponse.oldItemList().forEach(traineeId -> {
            UserCategoryEntity userCategory = userCategoryService.findByCategoryIdAndUserIdAndIsDeletedFalse(categoryId, traineeId);
            if (userCategory == null) {
                throw new NotFoundException(ResponseMessage.USER_CATEGORY_NOT_FOUND.name());
            }

            userCategoryService.softDelete(userCategory.getId());
        });
        categoryEntity = categoryRequest.toEntity(categoryEntity);
        CategoryResponse categoryResponse = CategoryResponse.fromEntity(update(categoryEntity));
        categoryResponse.setTraineeIdList(userCategoryService.findTraineeIdListByCategoryId(categoryEntity.getId()));
        return categoryResponse;
    }

    public List<Long> TraineeIdListWithModules(CategoryEntity categoryEntity) {
        List<Long> traineeIdList = new ArrayList<>();
        if (categoryEntity.getModules() != null) {
            CategoryResponse categoryResponse = CategoryResponse.fromEntity(categoryEntity);
           for (ModuleResponse module : categoryResponse.getModules()) {
               traineeIdList.addAll(userModuleService.findTraineeByModuleId(module.getId()));
           }
        }
        return traineeIdList;
    }

    public CategoryResponse getCategoryDetail(Integer id) {
        CategoryEntity categoryEntity = findByIdAndNotDeleted((long) id);
        if (categoryEntity == null) {
            throw new NotFoundException(ResponseMessage.CATEGORY_NOT_FOUND.name());
        }
        CategoryResponse categoryResponse = CategoryResponse.fromEntity(categoryEntity);
        String currentUserRole = getCurrentUser().getRole();
        if (currentUserRole.equals(Role.TRAINEE.name())) {
            categoryResponse.setTaskProcessId(userCategoryService.findTaskProcessIdByCategoryIdAndTraineeId((long) id, getCurrentUser().getUserId()));
        }
        List<ModuleResponse> moduleResponses = new ArrayList<>();
        if (categoryEntity.getModules() != null) {
            for (ModuleResponse moduleResponse : categoryResponse.getModules()) {
                if (currentUserRole.equals(Role.TRAINER.name()) || currentUserRole.equals(Role.ADMIN.name())) {
                    moduleResponse.setTraineeIdList(userModuleService.findTraineeByModuleId(moduleResponse.getId()));
                }

                if (currentUserRole.equals(Role.TRAINEE.name())) {
                    if (moduleResponse.getType().equals(Type.PRIVATE.toString()) &&
                            !userModuleService.findTraineeByModuleId(moduleResponse.getId()).
                                    contains(getCurrentUser().getUserId())
                    ) {
                        continue;
                    }
                }
                List<TaskResponse> taskResponses = new ArrayList<>();
                for (TaskResponse taskResponse : moduleResponse.getTasks()) {
                    if (currentUserRole.equals(Role.TRAINEE.name())) {
                        if (taskResponse.getType().equals(Type.PRIVATE.toString()) &&
                                !userTaskService.findTraineeByTaskId(taskResponse.getId()).
                                        contains(getCurrentUser().getUserId())
                        ) {
                            continue;
                        }
                    }
                    taskResponse.setTraineeIdList(userTaskService.findTraineeByTaskId(taskResponse.getId()));
                    taskResponses.add(taskResponse);
                }
                moduleResponse.setTasks(taskResponses);
                moduleResponses.add(moduleResponse);
            }
        }

        categoryResponse.setModules(moduleResponses);
        categoryResponse.setTraineeIdList(userCategoryService.findTraineeIdListByCategoryId((long) id));
        return categoryResponse;
    }

    public List<CategoryResponse> getCategoryList(CategoryFilter filter, String keyword) {
        String search = "";
        if (keyword != null) {
            search = keyword;
        }

        if (filter == null) {
            return repository.findByNameContainsIgnoreCaseAndIsDeletedFalse(search)
                    .stream().map(category -> {
                    CategoryResponse categoryResponse = CategoryResponse.fromEntity(category);
                    categoryResponse.setNumberOfTrainees(userCategoryService.findTraineeIdListByCategoryId(category.getId()).size());
                    categoryResponse.setCreatedBy(userService.findByIdAndNotDeleted(category.getCreatedByUserId()).getFullName());
                    return categoryResponse;
                })
                .toList();
        }

        List<Long> userIds = filter.getCreatedBy() != null
                ? userService.findUserIdsContainingFullName(filter.getCreatedBy())
                : Collections.emptyList();
        if (filter.getCreatedBy() != null && userIds.isEmpty()) {
            userIds = List.of(-1L);
        }

        List<Long> finalUserIds = userIds;
        Stream<CategoryEntity> filtered = repository.findByNameContainsIgnoreCaseAndIsDeletedFalse(search).stream()
                .filter(categoryEntity -> Optional.of(filter)
                        .map(CategoryFilter::getStatus)
                        .map(status -> status.contains(Status.valueOf(categoryEntity.getStatus())))
                        .orElse(true))
                .filter(categoryEntity -> finalUserIds.isEmpty() || finalUserIds.contains(categoryEntity.getCreatedByUserId()));
        return filtered.map(category -> {
                    CategoryResponse categoryResponse = CategoryResponse.fromEntity(category);
                    categoryResponse.setNumberOfTrainees(userCategoryService.findTraineeIdListByCategoryId(category.getId()).size());
                    categoryResponse.setCreatedBy(userService.findByIdAndNotDeleted(category.getCreatedByUserId()).getFullName());
                    return categoryResponse;
                })
                .toList();
    }

    public Page<CategoryResponse> getCategoryListByUserId(Long userId, Integer pageNumber, Integer pageSize) {
        int pageIndex = pageNumber - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        String currentUserRole = getCurrentUser().getRole();
        if ((currentUserRole.equals(Role.TRAINEE.name()) || currentUserRole.equals(Role.TRAINER.name())) && !getCurrentUser().getUserId().equals(userId)){
            throw new InvalidTokenException(ResponseMessage.USER_NOT_AUTHORIZED.toString());
        }

        return repository.findAllByUserId(userId, pageable)
                .map(CategoryResponse::fromEntity);
    }

    public List<SearchAllResponse> searchAll(String search) {
        List<SearchAllResponse> searchAllResponses = new ArrayList<>(taskService.searchAllTasks(search));
        List<CategoryEntity> categoryEntities = repository.findByNameContainsIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalse(search, search);
        searchAllResponses.addAll(categoryEntities.stream()
                .filter(searchAllResponse -> searchAllResponses.stream()
                        .map(SearchAllResponse::getId)
                        .noneMatch(searchAllResponse.getId()::equals))
                .map(SearchAllResponse::fromCategoryEntity)
                .toList());
        return searchAllResponses;
    }

    @Transactional
    public String updateTaskProcess(Integer taskId, Integer categoryId) {
        UserCategoryEntity userCategoryEntity = userCategoryService.findByCategoryIdAndUserIdAndIsDeletedFalse(
                (long) categoryId, getCurrentUser().getUserId());
        if (userCategoryEntity == null) {
            throw new NotFoundException(ResponseMessage.USER_CATEGORY_NOT_FOUND.name());
        }
        userCategoryEntity.setTaskProcessId((long) taskId);
        userCategoryService.update(userCategoryEntity);
        return ResponseMessage.TASK_PROCESS_UPDATED.name();
    }

    public Page<CategoryResponse> getCategoriesByCreatedUser(Long userId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber-1, pageSize);
//        return repository.findByCreatedByUserIdAndIsDeletedFalse(userId, pageable)
//                .map(CategoryResponse::fromEntity);

        return repository.findAssignedOrCreatedCategories(userId, pageable)
                .map(CategoryResponse::fromEntity);
    }

    public Page<CategoryResponse> getListSuggestions(Long userId, Integer pageNumber, Integer pageSize) {
        int pageIndex = pageNumber - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        String currentUserRole = getCurrentUser().getRole();
        if ((currentUserRole.equals(Role.TRAINEE.name()) || currentUserRole.equals(Role.TRAINER.name())) && !getCurrentUser().getUserId().equals(userId)){
            throw new InvalidTokenException(ResponseMessage.USER_NOT_AUTHORIZED.toString());
        }

        return repository.findListSuggestions(userId, pageable)
                .map(CategoryResponse::fromEntity);
    }

    public CategoryResponse changeCategoryStatus(Long categoryId, String status) {
        CategoryEntity categoryEntity = repository.findByIdAndIsDeletedFalse(categoryId).orElseThrow(
                () ->new NotFoundException(ResponseMessage.CATEGORY_NOT_FOUND.toString())
        );
        categoryEntity.setStatus(status);
        return CategoryResponse.fromEntity(update(categoryEntity));
    }

    @Transactional
    public CategoryResponse changeOrderId(Long categoryId, List<OrderRequest> orderRequests) {
        List<Long> modulesInCategory = Objects.requireNonNull(repository.findByIdAndIsDeletedFalse(categoryId)
                        .map(CategoryEntity::getModules)
                        .orElse(null))
                .stream()
                .map(ModuleEntity::getId)
                .toList();
        boolean allModulesExist = orderRequests.stream()
                .allMatch(orderRequest -> modulesInCategory.contains((long) orderRequest.getModuleId()));

        if (!allModulesExist) {
            throw new NotFoundException(ResponseMessage.MODULE_NOT_FOUND.toString());
        }

        for (int i = 0; i < orderRequests.size(); i++) {
           ModuleEntity moduleEntity = moduleService.findByIdAndNotDeleted(Long.valueOf(orderRequests.get(i).getModuleId()));
           moduleEntity.setOrderId(i+1);
           for (int j = 0; j < orderRequests.get(i).getTaskIds().size(); j++) {
               TaskEntity taskEntity = taskService.findByIdAndNotDeleted(Long.valueOf(orderRequests.get(i).getTaskIds().get(j)));
               taskEntity.setOrderId(j+1);
               taskService.update(taskEntity);
           }
           moduleService.update(moduleEntity);
        }
        return repository.findByIdAndIsDeletedFalse(categoryId).map(CategoryResponse::fromEntity).orElse(null);
    }
}
