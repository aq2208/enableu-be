package com.capstone.enableu.custom.response;

import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.util.Validate;
import lombok.Data;

import java.util.List;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String thumbnail;
    private String attachment;
    private String status;
    private String code;
    private List<ModuleResponse> modules;
    private List<Long> traineeIdList;
    private Long taskProcessId;
    private String createdAt;
    private String createdBy;
    private Long createdByUserId;
    private Integer numberOfModules;
    private Integer numberOfTrainees;
    private Integer numberOfTasks;

    public static CategoryResponse fromEntity(CategoryEntity categoryEntity) {
        int numberOfTasks = 0;
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryEntity.getId());
        categoryResponse.setCode(categoryEntity.getCode().toString());
        categoryResponse.setName(categoryEntity.getName());
        categoryResponse.setDescription(categoryEntity.getDescription());
        categoryResponse.setThumbnail(categoryEntity.getThumbnail());
        categoryResponse.setAttachment(categoryEntity.getAttachment());
        categoryResponse.setStatus(categoryEntity.getStatus());
        categoryResponse.setCreatedAt(Validate.convertDateToString(categoryEntity.getCreatedTime()));
        categoryResponse.setCreatedByUserId(categoryEntity.getCreatedByUserId());
        if (categoryEntity.getModules() != null) {
            List<ModuleResponse> moduleResponses = ModuleResponse.fromEntities(categoryEntity.getModules());
            categoryResponse.setModules(moduleResponses);
            categoryResponse.setNumberOfModules(moduleResponses.size());
            for (ModuleResponse moduleResponse : moduleResponses) {
                if (moduleResponse.getTasks() != null) {
                    numberOfTasks += moduleResponse.getTasks().size();
                }
            }
        }
        categoryResponse.setNumberOfTasks(numberOfTasks);
        return categoryResponse;
    }
}
