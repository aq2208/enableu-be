package com.capstone.enableu.custom.entity;

import com.capstone.enableu.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_module")
public class UserModuleEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @ManyToOne
    @JoinColumn(name = "module_id")
    private ModuleEntity module;
    private String status;
}
