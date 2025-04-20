package com.capstone.enableu.custom.entity;

import com.capstone.enableu.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.graphql.data.method.annotation.SchemaMapping;

import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
public class CategoryEntity extends BaseEntity {
    private String name;
    private String description;
    private String thumbnail;
    private String attachment;
    private String status;
    @OneToMany
    @JoinColumn(name = "category_id")
    private List<ModuleEntity> modules;

}
