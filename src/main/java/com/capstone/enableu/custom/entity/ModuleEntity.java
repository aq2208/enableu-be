package com.capstone.enableu.custom.entity;

import com.capstone.enableu.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Entity
@Getter
@Setter
@Table(name = "module")
public class ModuleEntity extends BaseEntity {
    private String name;
    private String type;
    private Integer orderId;
    private String status;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
    @OneToMany
    @JoinColumn(name = "module_id")
    private List<TaskEntity> tasks;

}
