package com.capstone.enableu.custom.request;

import com.capstone.enableu.custom.entity.CategoryEntity;
import com.capstone.enableu.custom.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CategoryRequest {
    @NotNull
    private String name;
    @NotNull
    private String description;
    private String thumbnail;
    private String attachment;
    @NotNull
    private String status;
    private List<Integer> traineeIdList;

    public CategoryEntity toEntity(CategoryEntity categoryEntity) {
        categoryEntity.setName(name);
        categoryEntity.setDescription(description);
        categoryEntity.setThumbnail(thumbnail);
        categoryEntity.setAttachment(attachment);
        categoryEntity.setStatus(status);
        return categoryEntity;
    }
}
