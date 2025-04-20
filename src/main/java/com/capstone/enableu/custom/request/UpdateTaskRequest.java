package com.capstone.enableu.custom.request;

import com.capstone.enableu.custom.dto.HighlightDuration;
import com.capstone.enableu.custom.entity.TaskEntity;
import com.capstone.enableu.custom.dto.StepInput;
import com.capstone.enableu.custom.enums.Type;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateTaskRequest {
    @NotNull
    private Long taskId;
    private String name;
    private String description;
    private String thumbnail;
    private String status;
    private String type;
    private int orderId;
    private String content;
    private Boolean isReviewMode;
    private List<StepInput> step;
    private List<Integer> traineeIdList;
    private List<HighlightDuration> highlightTimeJson;

    public TaskEntity updateTask(TaskEntity taskEntity) {
        taskEntity.setName(name);
        taskEntity.setDescription(description);
        taskEntity.setStatus(status);
        taskEntity.setThumbnail(thumbnail);
        taskEntity.setType(type);
        if (taskEntity.getModule() != null) {
            taskEntity.setIsReviewMode(isReviewMode);
        }
        taskEntity.setOrderId(orderId);

        if (type.equals(Type.PRIVATE.toString())) {
            String highlightJson = new Gson().toJson(highlightTimeJson);
            taskEntity.setHighlightTimeJson(highlightJson);
        }
        return taskEntity;
    }
}
