package com.capstone.enableu.custom.resolver;

import com.capstone.enableu.common.resolver.BaseResolver;
import com.capstone.enableu.custom.dto.CategoryFilter;
import com.capstone.enableu.custom.enums.Role;
import com.capstone.enableu.custom.request.CreateTaskRequest;
import com.capstone.enableu.custom.request.UpdateTaskRequest;
import com.capstone.enableu.custom.response.PaginationResponse;
import com.capstone.enableu.custom.response.TaskResponse;
import com.capstone.enableu.custom.service.StorageService;
import com.capstone.enableu.custom.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@AllArgsConstructor
public class TaskResolver extends BaseResolver<TaskService> {

    private final StorageService storageService;

    @MutationMapping(name = "createTask")
    @Secured(Role.TRAINER_CONSTANT)
    public TaskResponse createTask(@Argument(name = "input") CreateTaskRequest createTaskRequest) {
        return service.createTask(createTaskRequest);
    }

    @MutationMapping(name = "deleteTask")
    @Secured(Role.TRAINER_CONSTANT)
    public String deleteTask(@Argument Integer id) {
        return service.softDeleteTask(id);
    }

    @MutationMapping(name = "updateTask")
    @Secured(Role.TRAINER_CONSTANT)
    public TaskResponse updateTask(@Argument(name = "input") UpdateTaskRequest updateTask) {
        return service.updateTask(updateTask);
    }

    @QueryMapping(name="getTaskById")
    @Secured(Role.TRAINEE_CONSTANT)
    public TaskResponse getTaskById(@Argument("id") Long id) {
        return service.getTask(id);
    }

    @QueryMapping(name="getTasksByModuleId")
    @Secured(Role.TRAINEE_CONSTANT)
    public PaginationResponse<TaskResponse> getTasksByModuleId(@Argument Long moduleId, @Argument Integer pageNumber, @Argument Integer pageSize) {
        return PaginationResponse.of(service.getTaskByModuleId(moduleId, pageNumber, pageSize));
    }

    @QueryMapping(name = "getRandomTasksByUserId")
    public  PaginationResponse<TaskResponse> getRandomTasksByUserId(@Argument Long userId, @Argument Boolean isCurrentTime, @Argument Integer pageNumber, @Argument Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return PaginationResponse.of(service.getTaskByUserId(userId, isCurrentTime), pageable);
    }

    @QueryMapping(name = "getRandomTasks")
    @Secured(Role.TRAINEE_CONSTANT)
    public PaginationResponse<TaskResponse> getRandomTasksList(@Argument CategoryFilter filter, @Argument String keyword, @Argument Integer pageNumber, @Argument Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return PaginationResponse.of(service.getRandomTasks(filter, keyword), pageable);
    }

    @MutationMapping(name="updateTaskStatus")
    @Secured(Role.TRAINER_CONSTANT)
    public TaskResponse updateTaskStatus(@Argument(name = "id") Long id, @Argument(name = "status") String status) {
        return service.updateStatus(id, status);
    }

    @QueryMapping(name="getRandomTasksByCreatedUser")
    @Secured(Role.TRAINER_CONSTANT)
    public PaginationResponse<TaskResponse> getRandomTasksByCreatedUser(@Argument Long createdUserId, @Argument Integer pageNumber, @Argument Integer pageSize) {
        return PaginationResponse.of(service.getRandomTasksByCreatedUser(createdUserId, pageNumber, pageSize));
    }

    @QueryMapping(name="getNextTaskId")
    public long getNextTaskId(@Argument Long taskId) {
       return  service.getNextTaskId(taskId);
    }

    @QueryMapping(name="getPreviousTaskId")
    public long getPreviousTaskId(@Argument Long taskId) {
        return  service.getPreviousTaskId(taskId);
    }

    @QueryMapping(name="getAllReviewModeTasksByCreatedUser")
    public List<TaskResponse> getAllReviewModeTasksByCreatedUser(@Argument Long createdUserId) {
        return service.getAllReviewModeTasksByCreatedUser(createdUserId);
    }

    @QueryMapping(name = "getAllReviewModeTasksByUserId")
    public List<TaskResponse> getAllReviewModeTasksByUserId(@Argument Long userId) {
        return service.getAllReviewModeTasksByUserId(userId);
    }

    @QueryMapping(name = "getCategoryId")
    @Secured(Role.TRAINEE_CONSTANT)
    public int getCategoryId(@Argument Long taskId) {
        return service.getCategoryId(taskId);
    }
}
