package com.capstone.enableu.custom.request;

import com.capstone.enableu.custom.dto.HighlightDuration;
import com.capstone.enableu.custom.entity.TaskEntity;
import com.capstone.enableu.custom.dto.StepInput;
import com.capstone.enableu.custom.enums.Type;
import com.capstone.enableu.custom.util.Validate;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
public class CreateTaskRequest {
    @NotNull
    private String name;
    @NotNull
    private String description;
    private String thumbnail;
    private String attachment;
    @NotNull
    private String status;
    private String type;
    private int orderId;
    private Long moduleId;
    private Boolean isReviewMode;
    private String content;
    private List<StepInput> step;
    //update later when doing random task
    private List<HighlightDuration> highlightTimeJson;
    private List<Integer> traineeIdList;

    public TaskEntity createTask() {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setName(name);
        taskEntity.setDescription(description);
        taskEntity.setStatus(status);
        taskEntity.setThumbnail(thumbnail);
        taskEntity.setAttachment(attachment);
        taskEntity.setType(type);
        if (moduleId != null) {
            taskEntity.setIsReviewMode(isReviewMode != null && isReviewMode);
        } else {
            taskEntity.setIsReviewMode(false);
        }
//        taskEntity.setOrderId(orderId);

        if (type.equals(Type.PRIVATE.toString())) {
            if (highlightTimeJson != null) {
                Validate.validateListHighlightDuration(highlightTimeJson);
                String highlightJson = new Gson().toJson(highlightTimeJson);
                taskEntity.setHighlightTimeJson(highlightJson);
            }
        }

        return taskEntity;
    }
}

