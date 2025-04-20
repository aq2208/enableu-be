package com.capstone.enableu.custom.service;

import com.capstone.enableu.common.service.BaseService;
import com.capstone.enableu.custom.entity.*;
import com.capstone.enableu.custom.enums.ResponseMessage;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.enums.Status;
import com.capstone.enableu.custom.enums.Type;
import com.capstone.enableu.custom.exception.BadRequestException;
import com.capstone.enableu.custom.exception.NotFoundException;
import com.capstone.enableu.custom.repository.CategoryRepository;
import com.capstone.enableu.custom.repository.ModuleRepository;
import com.capstone.enableu.custom.request.ModuleRequest;
import com.capstone.enableu.custom.response.ListCompareResponse;
import com.capstone.enableu.custom.response.ModuleResponse;
import com.capstone.enableu.custom.response.PaginationResponse;
import com.capstone.enableu.custom.response.TaskResponse;
import com.capstone.enableu.custom.util.ListCompare;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ModuleService extends BaseService<ModuleEntity, ModuleRepository> {

    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final UserModuleService userModuleService;

    private final TaskService taskService;
    private final java.util.logging.Logger log = java.util.logging.Logger.getLogger(this.getClass().getName());
    private final UserTaskService userTaskService;

    @Transactional
    public ModuleResponse createModule(Integer categoryId, ModuleRequest moduleRequest) {
        ModuleEntity moduleEntity = new ModuleEntity();
        moduleEntity = moduleRequest.toEntity(moduleEntity);
        CategoryEntity categoryEntity = categoryRepository.findByIdAndIsDeletedFalse((long) categoryId).orElse(null);
        if (categoryEntity == null) {
            throw new NotFoundException(ResponseMessage.MODULE_NOT_FOUND.name());
        }
        moduleEntity.setCategory(categoryEntity);
        ModuleEntity newModuleEntity = save(moduleEntity);
        if (Objects.equals(moduleRequest.getType(), Type.PUBLIC.toString())) {
            return ModuleResponse.fromEntity(newModuleEntity);
        }
        userModuleService.createBulkWithListUserIds(newModuleEntity, ListCompare.convertIntegerToLongList(moduleRequest.getTraineeIdList()));
        ModuleResponse moduleResponse = ModuleResponse.fromEntity(moduleEntity);
        moduleResponse.setTraineeIdList(userModuleService.findTraineeByModuleId(newModuleEntity.getId()));

        return moduleResponse;
    }

    @Transactional
    public ModuleResponse updateModule(Integer moduleId, ModuleRequest moduleRequest) {
        Long id = (long) moduleId;
        ModuleEntity moduleEntity = findByIdAndNotDeleted(id);
        if (moduleEntity == null) {
            throw new NotFoundException(ResponseMessage.MODULE_NOT_FOUND.name());
        }
        moduleEntity = save(moduleRequest.toEntity(moduleEntity));
        if (Objects.equals(moduleRequest.getType(), Type.PUBLIC.toString())) {
           return ModuleResponse.fromEntity(moduleEntity);
        }
        ListCompareResponse listCompareResponse = ListCompare.compareLists(
                userModuleService.findTraineeByModuleId((long)moduleId),
                ListCompare.convertIntegerToLongList(moduleRequest.getTraineeIdList())
        );

        userModuleService.createBulkWithListUserIds(moduleEntity, listCompareResponse.newItemList());

        List<Long> traineeIdList = TraineeIdListWithTasks(moduleEntity);

        if (new HashSet<>(traineeIdList).stream().anyMatch(listCompareResponse.oldItemList()::contains)) {
            throw new BadRequestException(ResponseMessage.TRAINEE_ID_IS_USED.name());
        }

        userModuleService.findByModuleIdAndUserId(Long.valueOf(moduleId), listCompareResponse.oldItemList()).forEach(
                userModuleEntity -> {
                    userModuleEntity.setDeleted(true);
                    userModuleService.update(userModuleEntity);
                }
        );

        ModuleResponse moduleResponse = ModuleResponse.fromEntity(moduleEntity);
        moduleResponse.setTraineeIdList(userModuleService.findTraineeByModuleId(id));

        return moduleResponse;
    }


    public List<Long> TraineeIdListWithTasks(ModuleEntity moduleEntity) {
        List<Long> traineeIdList = new ArrayList<>();
        if (moduleEntity.getTasks() != null) {
            ModuleResponse moduleResponse = ModuleResponse.fromEntity(moduleEntity);
            for (TaskResponse taskEntity : moduleResponse.getTasks()) {
                if (Objects.equals(taskEntity.getType(), Type.PRIVATE.toString())) {
                    traineeIdList.addAll(userTaskService.findTraineeByTaskId(taskEntity.getId()));
                }
            }
        }
        return traineeIdList;
    }

    public String softDeleteModule(Integer id) {
        Long moduleId = (long) id;
        ModuleEntity moduleEntity = findByIdAndNotDeleted(moduleId);
        boolean deleted = softDelete(moduleId);
        if (!deleted) {
            throw new BadRequestException(ResponseMessage.MODULE_DELETED_FAILED.name());
        }
        List<TaskEntity> taskList = findById(moduleId).getTasks();
        if (!Objects.isNull(taskList)) {
            for (TaskEntity task : taskList) {
                taskService.softDelete(task.getId());
            }
        }

        if (Objects.equals(moduleEntity.getType(), Type.PRIVATE.toString())) {
           userModuleService.deleteUserModuleByModuleId(moduleId);
        }
        return ResponseMessage.MODULE_DELETED_SUCCESSFULLY.name();
    }

    public ModuleResponse getModuleDetail(Integer id) {
        Long moduleId = (long) id;
        ModuleEntity moduleEntity = findByIdAndNotDeleted(moduleId);
        if (moduleEntity == null) {
            throw new NotFoundException(ResponseMessage.MODULE_NOT_FOUND.name());
        }
        ModuleResponse moduleResponse = ModuleResponse.fromEntity(moduleEntity);
        String currentUserRole = getCurrentUser().getRole();
        if (currentUserRole.equals(Role.TRAINEE.name()) || currentUserRole.equals(Role.ADMIN.name())) {
           moduleResponse.setTraineeIdList(userModuleService.findTraineeByModuleId(moduleId));
        }

        return moduleResponse;
    }

    public Page<ModuleResponse> getModuleListOfCategory(Integer categoryId, Integer pageNumber, Integer pageSize) {
        Integer pageIndex = pageNumber - 1;
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return repository.findByCategoryIdAndIsDeletedFalse((long) categoryId ,pageable)
                .map(ModuleResponse::fromEntity);
    }
}
