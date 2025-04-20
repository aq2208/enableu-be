package com.capstone.enableu.custom.request;

import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.entity.ModuleEntity;
import lombok.Data;

import java.util.List;

@Data
public class ModuleRequest {
    private String name;
    private String type;
    private String status;
    private Integer orderId;
    private List<Integer> traineeIdList;

    public ModuleEntity toEntity(ModuleEntity moduleEntity) {
        moduleEntity.setName(name);
        moduleEntity.setType(type);
        moduleEntity.setOrderId(orderId);
        moduleEntity.setStatus(status);
        return moduleEntity;
    }
}
