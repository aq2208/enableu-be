package com.capstone.enableu.custom.entity;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.custom.enums.Status;
import com.capstone.enableu.custom.enums.Type;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "task")
public class TaskEntity extends BaseEntity {
    private String name;
    private String thumbnail;
    private String attachment;
    private String type;
    private String description;
    private Integer orderId;
    private String highlightTimeJson;
    private String content;
    private Boolean isReviewMode;
    @ManyToOne
    @JoinColumn(name = "module_id")
    private ModuleEntity module;
    private String status;
}
