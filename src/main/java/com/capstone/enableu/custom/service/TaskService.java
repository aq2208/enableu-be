package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.dto.CategoryFilter;
import com.capstone.enableu.custom.dto.StepInput;
import com.capstone.enableu.custom.entity.*;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.enums.Status;
import com.capstone.enableu.custom.enums.Type;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.ModuleRepository;
import com.capstone.enableu.custom.repository.TaskRepository;
import com.capstone.enableu.custom.request.CreateTaskRequest;
import com.capstone.enableu.custom.request.UpdateTaskRequest;
import com.capstone.enableu.custom.response.*;
import com.capstone.enableu.custom.util.ListCompare;
import com.capstone.enableu.custom.util.TextToSpeech;
import com.capstone.enableu.custom.util.Validate;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class TaskService extends BaseService<TaskEntity, TaskRepository> {

    private final ModuleRepository moduleRepository;
    private final UserService userService;
    private final UserTaskService userTaskService;
    private final UserCategoryService userCategoryService;
    private final TextToSpeech textToSpeech;
    private final UserModuleService userModuleService;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest taskRequest) {
        TaskEntity taskEntity = taskRequest.createTask();
        taskEntity.setOrderId(getNextTaskOrderId(taskRequest.getModuleId()));
        if (taskRequest.getStep() != null) {
            taskEntity.setContent(getStepAttachment(taskRequest.getStep()));
        }
        if (taskRequest.getModuleId() != null) {
            ModuleEntity moduleEntity = moduleRepository.findByIdAndIsDeletedFalse(taskRequest.getModuleId())
                    .orElse(null);
            if (moduleEntity == null) {
                throw new NotFoundException(ResponseMessage.MODULE_NOT_FOUND.name());
            }
            taskEntity.setModule(moduleEntity);
        }
        taskEntity = save(taskEntity);
        TaskEntity saveTaskEntity = taskEntity;
        if (taskRequest.getType().equals(Type.PRIVATE.toString())) {
            userTaskService.createBulkWithListUserIds(saveTaskEntity,
                    ListCompare.convertIntegerToLongList(taskRequest.getTraineeIdList()));
        }
        TaskResponse taskResponse = TaskResponse.fromEntity(taskEntity);
        taskResponse.setTraineeIdList(userTaskService.findTraineeByTaskId(saveTaskEntity.getId()));

        return taskResponse;
    }

    public TaskResponse getTask(Long taskId) {
        TaskEntity taskEntity = findByIdAndNotDeleted(taskId);
        if (taskEntity == null) {
            throw new NotFoundException(ResponseMessage.TASK_NOT_FOUND.name());
        }
        if (Objects.equals(taskEntity.getType(), Type.PUBLIC.toString())) {
            if (Objects.equals(getCurrentUser().getRole(), Role.TRAINEE.name()) &&
                    !userCategoryService.findTraineeIdListByCategoryId(taskEntity.getModule().getCategory().getId())
                            .contains(getCurrentUser().getUserId())) {
                try {
                    throw new AuthenticationException(ResponseMessage.USER_NOT_AUTHORIZED.name());
                } catch (AuthenticationException e) {
                    throw new RuntimeException(e);
                }
            }

            return TaskResponse.fromEntity(taskEntity);
        }

        TaskResponse taskResponse = TaskResponse.fromEntity(taskEntity);
        if (Objects.equals(getCurrentUser().getRole(), Role.TRAINEE_CONSTANT) &&
                !userTaskService.findTraineeByTaskId(taskEntity.getId()).contains(getCurrentUser().getUserId())) {
            try {
                throw new AuthenticationException(ResponseMessage.USER_NOT_AUTHORIZED.name());
            } catch (AuthenticationException e) {
                throw new RuntimeException(e);
            }
        }
        taskResponse.setTraineeIdList(userTaskService.findTraineeByTaskId(taskEntity.getId()));
        return taskResponse;
    }

    public String softDeleteTask(Integer id) {
        Long taskId = (long) id;
        TaskEntity taskEntity = findByIdAndNotDeleted(taskId);
        if (taskEntity == null) {
            throw new NotFoundException(ResponseMessage.TASK_NOT_FOUND.name());
        }

        boolean deleted = softDelete(taskId);
        if (!deleted) {
            throw new NotFoundException(ResponseMessage.TASK_DELETED_FAILED.name());
        }

        if (taskEntity.getType().equals(Type.PUBLIC.toString())) {
            return ResponseMessage.TASK_DELETED_SUCCESSFULLY.name();
        }
        List<UserTaskEntity> userTaskEntities = userTaskService.findByTaskId(taskId);
        userTaskEntities.forEach(userTaskEntity -> {
            userTaskEntity.setDeleted(true);
            userTaskService.save(userTaskEntity);
        });

        return ResponseMessage.TASK_DELETED_SUCCESSFULLY.name();
    }

    public TaskResponse updateTask(UpdateTaskRequest updateTaskRequest) {
        Long taskId = updateTaskRequest.getTaskId();

        TaskEntity taskEntity = findByIdAndNotDeleted(taskId);
        if (taskEntity == null) {
            throw new NotFoundException(ResponseMessage.TASK_NOT_FOUND.name());
        }
        taskEntity = updateTaskRequest.updateTask(taskEntity);
        if (updateTaskRequest.getStep() != null) {
            taskEntity.setContent(getStepAttachment(updateTaskRequest.getStep()));
        }
        if (Objects.equals(taskEntity.getType(), Type.PUBLIC.toString())) {
            return TaskResponse.fromEntity(update(taskEntity));
        }
        ListCompareResponse listCompareResponse = ListCompare.compareLists(
                userTaskService.findTraineeByTaskId(taskId),
                ListCompare.convertIntegerToLongList(updateTaskRequest.getTraineeIdList()));
        userTaskService.createBulkWithListUserIds(taskEntity, listCompareResponse.newItemList());
        userTaskService.findByTaskIdAndUserIds(taskId, listCompareResponse.oldItemList()).forEach(
                updateTaskEntity -> {
                    updateTaskEntity.setDeleted(true);
                    userTaskService.update(updateTaskEntity);
                });
        TaskResponse taskResponse = TaskResponse.fromEntity(taskEntity);
        taskResponse.setTraineeIdList(userTaskService.findTraineeByTaskId(taskId));

        return TaskResponse.fromEntity(update(taskEntity));
    }

    public List<SearchAllResponse> searchAllTasks(String searchText) {

        List<TaskEntity> taskEntities = repository
                .findByNameContainsIgnoreCaseAndIsDeletedFalseOrDescriptionContainingIgnoreCaseAndIsDeletedFalse(
                        searchText, searchText);
        Map<Long, SearchAllResponse> responseMap = new HashMap<>();
        List<SearchAllResponse> searchAllResponses = new ArrayList<>();

        for (TaskEntity taskEntity : taskEntities) {
            SearchAllResponse categoryEntity = Optional.ofNullable(taskEntity.getModule())
                    .map(ModuleEntity::getCategory)
                    .map(SearchAllResponse::fromCategoryEntity)
                    .orElse(null);
            if (categoryEntity == null) {
                if (taskEntity.getType().equals(Type.PRIVATE.toString())) {
                    searchAllResponses.add(SearchAllResponse.fromTaskEntity(taskEntity));
                }
                continue;
            }
            SearchAllResponse res = Optional.ofNullable(responseMap.get(categoryEntity.getId()))
                    .orElse(categoryEntity);
            List<SearchAllResponse.TaskSearchResponse> tasksRes = Optional.ofNullable(res.getTasks())
                    .orElse(new ArrayList<>());
            tasksRes.add(SearchAllResponse.TaskSearchResponse.fromTaskEntity(taskEntity));
            res.setTasks(tasksRes);
            responseMap.put(categoryEntity.getId(), res);
        }
        searchAllResponses.addAll(responseMap.values().stream().toList());
        return searchAllResponses;
    }

    public Page<TaskResponse> getTaskByModuleId(Long moduleId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TaskEntity> taskEntityPage = repository.findByModuleIdAndIsDeletedFalse(moduleId, pageable);
        return taskEntityPage.map(TaskResponse::fromEntity);
    }

    public List<TaskResponse> getTaskByUserId(Long userId, Boolean isCurrentTime) {
        if (Objects.equals(getCurrentUser().getRole(), Role.TRAINEE_CONSTANT)
                && !Objects.equals(userId, getCurrentUser().getUserId())) {
            try {
                throw new AuthenticationException(ResponseMessage.USER_NOT_AUTHORIZED.toString());
            } catch (AuthenticationException e) {
                throw new RuntimeException(e);
            }
        }
        List<TaskResponse> taskResponses = userTaskService.findByUserIdAndIsDeletedFalse(userId).stream()
                .map(UserTaskEntity::getTask)
                .filter(taskEntity -> taskEntity.getType().equals(Type.PRIVATE.toString()) &&
                        taskEntity.getModule() == null &&
                        !Objects.equals(taskEntity.getStatus(), Status.INACTIVE.name()))
                .map(TaskResponse::fromEntity).toList();

        if (!isCurrentTime) {
            return taskResponses;
        }

        return taskResponses.stream()
                .filter(taskResponse -> Validate.isCurrentTimeInAnyRange(taskResponse.getHighlightTimeJson()))
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getRandomTasks(CategoryFilter filter, String keyword) {
        String searchText = "";
        if (keyword != null) {
            searchText = keyword;
        }

        if (filter == null) {
            return repository
                    .findByNameContainsIgnoreCaseAndModuleNullAndTypeContainingAndIsDeletedFalse(searchText,
                            Type.PRIVATE.name())
                    .stream().map(taskEntity -> {
                        TaskResponse taskResponse = TaskResponse.fromEntity(taskEntity);
                        taskResponse.setCreatedBy(
                                userService.findByIdAndNotDeleted(taskResponse.getCreatedByUserId()).getFullName());
                        taskResponse
                                .setNumberOfTrainees(userTaskService.findTraineeByTaskId(taskResponse.getId()).size());
                        return taskResponse;
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
        Stream<TaskEntity> filtered = repository
                .findByNameContainsIgnoreCaseAndModuleNullAndTypeContainingAndIsDeletedFalse(searchText,
                        Type.PRIVATE.name())
                .stream()
                .filter(taskEntity -> Optional.of(filter)
                        .map(CategoryFilter::getStatus)
                        .map(status -> status.contains(Status.valueOf(taskEntity.getStatus())))
                        .orElse(true))
                .filter(categoryEntity -> finalUserIds.isEmpty()
                        || finalUserIds.contains(categoryEntity.getCreatedByUserId()));
        return filtered.map(taskEntity -> {
            TaskResponse taskResponse = TaskResponse.fromEntity(taskEntity);
            taskResponse
                    .setCreatedBy(userService.findByIdAndNotDeleted(taskResponse.getCreatedByUserId()).getFullName());
            taskResponse.setNumberOfTrainees(userTaskService.findTraineeByTaskId(taskResponse.getId()).size());
            return taskResponse;
        })
                .toList();
    }

    public Page<TaskResponse> getRandomTasksByCreatedUser(Long createdUserId, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        return repository.findByCreatedByUserIdAndTypeContainingAndIsDeletedFalse(createdUserId, "PRIVATE", pageable)
                .map(TaskResponse::fromEntity);
    }

    public TaskResponse updateStatus(Long taskId, String status) {
        TaskEntity taskEntity = findByIdAndNotDeleted(taskId);
        if (taskEntity == null) {
            throw new NotFoundException(ResponseMessage.TASK_NOT_FOUND.toString());
        }
        taskEntity.setStatus(status);
        return TaskResponse.fromEntity(save(taskEntity));
    }

    public long getNextTaskId(Long taskId) {
        TaskEntity taskEntity = findByIdAndNotDeleted(taskId);
        ModuleEntity moduleEntity = taskEntity.getModule();
        ModuleResponse moduleResponse = ModuleResponse.fromEntity(moduleEntity);
        int orderNext = taskEntity.getOrderId() + 1;
        for (TaskResponse taskResponse : moduleResponse.getTasks()) {
            if (taskResponse.getOrderId().equals(orderNext)) {
                if (taskResponse.getType().equals(Type.PUBLIC.toString())
                        || (userTaskService.findTraineeByTaskId(taskResponse.getId())
                                .contains(getCurrentUser().getUserId())
                                && taskResponse.getType().equals(Type.PRIVATE.toString()))) {
                    return taskResponse.getId();
                }

                orderNext++;
            }
        }
        ModuleResponse nextModule = getNextModule(moduleEntity);
        int minOrder = 1;
        if (nextModule != null) {
            for (TaskResponse task : nextModule.getTasks()) {
                if (task.getOrderId().equals(minOrder)) {
                    if (task.getType().equals(Type.PUBLIC.toString())
                            || (task.getType().equals(Type.PRIVATE.toString()) &&
                                    userTaskService.findTraineeByTaskId(task.getId())
                                            .contains(getCurrentUser().getUserId()))) {
                        return task.getId();
                    }
                }
                minOrder++;
            }
        }
        return 0;
    }

    public long getPreviousTaskId(Long taskId) {
        TaskEntity taskEntity = findByIdAndNotDeleted(taskId);
        ModuleEntity moduleEntity = taskEntity.getModule();
        ModuleResponse moduleResponse = ModuleResponse.fromEntity(moduleEntity);
        int orderNext = taskEntity.getOrderId() - 1;
        if (orderNext > 0) {
            for (TaskResponse taskResponse : moduleResponse.getTasks()) {
                if (taskResponse.getOrderId().equals(orderNext)) {
                    if (taskResponse.getType().equals(Type.PUBLIC.toString())) {
                        return taskResponse.getId();
                    }
                    if (taskResponse.getTraineeIdList() != null && userTaskService
                            .findTraineeByTaskId(taskResponse.getId()).contains(getCurrentUser().getUserId())) {
                        return taskResponse.getId();
                    }
                    orderNext--;
                }
            }
        }
        ModuleResponse preModuleResponse = getPreviousModule(moduleEntity);
        if (preModuleResponse != null) {
            TaskResponse preTask = new TaskResponse();
            for (TaskResponse task : preModuleResponse.getTasks()) {
                if (preTask.getOrderId() < task.getOrderId() &&
                        (task.getType().equals(Type.PUBLIC.toString()) ||
                                (task.getType().equals(Type.PRIVATE.toString())
                                        && userTaskService.findTraineeByTaskId(task.getId())
                                                .contains(getCurrentUser().getUserId())))) {
                    preTask = task;
                }
            }
            return preTask.getId();
        }
        return 0;
    }

    public ModuleResponse getNextModule(ModuleEntity moduleEntity) {
        CategoryEntity categoryEntity = moduleEntity.getCategory();
        CategoryResponse categoryResponse = CategoryResponse.fromEntity(categoryEntity);
        int orderNext = moduleEntity.getOrderId() + 1;
        for (ModuleResponse module : categoryResponse.getModules()) {
            if (module.getOrderId().equals(orderNext)) {
                if (module.getType().equals(Type.PUBLIC.toString()) ||
                        (userModuleService.findTraineeByModuleId(module.getId())
                                .contains(getCurrentUser().getUserId())
                                && module.getType().equals(Type.PRIVATE.toString()))) {
                    for (TaskResponse task : module.getTasks()) {
                        if (task.getType().equals(Type.PUBLIC.toString())
                                || (task.getType().equals(Type.PRIVATE.toString())
                                        && userTaskService.findTraineeByTaskId(task.getId())
                                                .contains(getCurrentUser().getUserId()))) {
                            return module;
                        }
                    }
                }
                orderNext++;
            }
        }
        return null;
    }

    public ModuleResponse getPreviousModule(ModuleEntity moduleEntity) {
        CategoryEntity categoryEntity = moduleEntity.getCategory();
        CategoryResponse categoryResponse = CategoryResponse.fromEntity(categoryEntity);
        int orderNext = moduleEntity.getOrderId() - 1;
        for (ModuleResponse module : categoryResponse.getModules()) {
            if (module.getOrderId().equals(orderNext)) {
                if (module.getType().equals(Type.PUBLIC.toString())) {
                    return module;
                }
                if (userModuleService.findTraineeByModuleId(module.getId()).contains(getCurrentUser().getUserId())) {
                    return module;
                }
                orderNext--;
            }
        }
        return null;
    }

    public List<TaskResponse> getAllReviewModeTasksByCreatedUser(Long createdUserId) {
        if (!Objects.equals(createdUserId, getCurrentUser().getUserId())
                && Objects.equals(getCurrentUser().getRole(), Role.TRAINEE_CONSTANT)) {
            try {
                throw new AuthenticationException(ResponseMessage.USER_NOT_AUTHORIZED.name());
            } catch (AuthenticationException e) {
                throw new RuntimeException(e);
            }
        }
        return repository.findByCreatedByUserIdAndIsReviewModeTrueAndIsDeletedFalse(createdUserId).stream()
                .map(TaskResponse::fromEntity).collect(Collectors.toList());
    }

    public List<TaskResponse> getAllReviewModeTasksByUserId(Long userId) {
        if (!Objects.equals(userId, getCurrentUser().getUserId())
                && Objects.equals(getCurrentUser().getRole(), Role.TRAINEE_CONSTANT)) {
            try {
                throw new AuthenticationException(ResponseMessage.USER_NOT_AUTHORIZED.name());
            } catch (AuthenticationException e) {
                throw new RuntimeException(e);
            }
        }
        List<TaskResponse> taskResponses = new ArrayList<>();
        taskResponses.addAll(
                userCategoryService.findByUserIdAndIsDeletedFalse(userId)
                        .stream()
                        .map(UserCategoryEntity::getCategory)
                        .map(CategoryEntity::getModules)
                        .flatMap(Collection::stream) // Flatten modules into a single stream
                        .map(ModuleResponse::fromEntity) // Map to ModuleResponse
                        .filter(moduleResponse -> Objects.equals(moduleResponse.getType(), Type.PUBLIC.toString()) ||
                                userModuleService.findTraineeByModuleId(moduleResponse.getId()).contains(userId)) 
                        .flatMap(moduleResponse -> moduleResponse.getTasks().stream()
                                .filter(taskResponse -> taskResponse.getIsReviewMode()
                                        && (taskResponse.getType().equals(Type.PUBLIC.toString()) ||
                                        userTaskService.findTraineeByTaskId(taskResponse.getId()).contains(userId))))
                        .toList()
        );

        return taskResponses;
    }

    private Integer getNextTaskOrderId(Long moduleId) {
        Integer maxOrderId = repository.findMaxOrderIdByModuleId(moduleId);
        return maxOrderId == null ? 1 : maxOrderId + 1;
    }

    public String getStepAttachment(List<StepInput> stepInput) {
        for (StepInput stepIn : stepInput) {
            if (stepIn.getText() != null) {
                // stepIn.setAttachmentUrl(textToSpeech.getAsyncLink(stepIn.getText()));
                // stepIn.setAttachmentUrl("");
            }
        }
        return new Gson().toJson(stepInput);
    }

    public int getCategoryId(Long taskId) {
        TaskEntity taskEntity = findByIdAndNotDeleted(taskId);
        if (taskEntity.getModule() != null) {
            return Math.toIntExact(taskEntity.getModule().getCategory().getId());
        }
        return 0;
    }
}
