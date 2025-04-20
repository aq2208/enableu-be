package com.capstone.enableu.custom.response;

import com.capstone.enableu.custom.entity.ModuleEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Data
public class ModuleResponse {
    private Long id;
    private String name;
    private String type;
    private String status;
    private Integer orderId;
    private String code;
    private List<TaskResponse> tasks;
    private List<Long> traineeIdList;

    public static ModuleResponse fromEntity(ModuleEntity moduleEntity) {
        ModuleResponse moduleResponse = new ModuleResponse();
        moduleResponse.setId(moduleEntity.getId());
        moduleResponse.setName(moduleEntity.getName());
        moduleResponse.setType(moduleEntity.getType());
        moduleResponse.setStatus(moduleEntity.getStatus());
        moduleResponse.setOrderId(moduleEntity.getOrderId());
        moduleResponse.setCode(moduleEntity.getCode().toString());
        if (moduleEntity.getTasks() != null) {
            moduleResponse.setTasks(TaskResponse.fromEntities(moduleEntity.getTasks()));
        }
        return moduleResponse;
    }

    public static List<ModuleResponse> fromEntities(List<ModuleEntity> moduleEntities) {
        if (Objects.isNull(moduleEntities)) {
            return null;
        }

        return moduleEntities.stream()
                .filter(taskEntity -> !taskEntity.isDeleted())
                .map(ModuleResponse::fromEntity)
                .sorted(Comparator.comparing(ModuleResponse::getOrderId,
                        Comparator.nullsFirst(Integer::compareTo)))
                .toList();
    }
}
