package com.capstone.enableu.custom.response;

import com.capstone.enableu.custom.dto.HighlightDuration;
import com.capstone.enableu.custom.entity.TaskEntity;
import com.capstone.enableu.custom.util.Validate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Data
public class TaskResponse {
    private Long id;
    private String name;
    private String type;
    private String status;
    private Integer orderId;
    private String code;
    private String description;
    private String attachment;
    private String thumbnail;
    private String content;
    private List<StepResponse> step;
    @NoArgsConstructor()
    @Getter
    @Setter
    public class StepResponse {
        String name;
        String content;
        Integer orderId;
        String text;
        String attachmentUrl;
    }
    private Boolean isReviewMode;
    private Long moduleId;
    private List<HighlightDuration> highlightTimeJson;
    private List<Long> traineeIdList;
    private String createdAt;
    private String createdBy;
    private Integer numberOfTrainees;
    private Long createdByUserId;

    public static TaskResponse fromEntity(TaskEntity taskEntity) {
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setId(taskEntity.getId());
        taskResponse.setName(taskEntity.getName());
        taskResponse.setType(taskEntity.getType());
        taskResponse.setStatus(taskEntity.getStatus());
        taskResponse.setIsReviewMode(taskEntity.getIsReviewMode());
        taskResponse.setOrderId(taskEntity.getOrderId());
        taskResponse.setCode(taskEntity.getCode().toString());
        taskResponse.setDescription(taskEntity.getDescription());
        taskResponse.setAttachment(taskEntity.getAttachment());
        taskResponse.setThumbnail(taskEntity.getThumbnail());
        taskResponse.setCreatedAt(Validate.convertDateToString(taskEntity.getCreatedTime()));
        taskResponse.setCreatedByUserId(taskEntity.getCreatedByUserId());

        if (taskEntity.getModule() != null) {
            taskResponse.setModuleId(taskEntity.getModule().getId());
        }

        Type listType = new TypeToken<List<StepResponse>>() {}.getType();
        Gson gson = new Gson();
        List<StepResponse> stepResponses = gson.fromJson(taskEntity.getContent(), listType);
        taskResponse.setStep(stepResponses);

        if (Objects.equals(taskEntity.getType(), "PRIVATE")){
            Type listHighlightType = new TypeToken<List<HighlightDuration>>() {}.getType();
            List<HighlightDuration> highlightDurations = gson.fromJson(taskEntity.getHighlightTimeJson(), listHighlightType);
            taskResponse.setHighlightTimeJson(highlightDurations);
        }

        return taskResponse;
    }

    public static List<TaskResponse> fromEntities(List<TaskEntity> taskEntities) {
        if (Objects.isNull(taskEntities)) {
            return null;
        }

        return taskEntities.stream()
                .filter(taskEntity -> !taskEntity.isDeleted()) // Skip deleted tasks
                .map(TaskResponse::fromEntity) // Convert to TaskResponse
                .sorted(Comparator.comparing(TaskResponse::getOrderId, Comparator.nullsLast(Integer::compareTo))) // Sort by orderId, handling nulls
                .toList(); // Collect sorted list
    }
}
