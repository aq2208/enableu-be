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
@Table(name = "user_category")
public class UserCategoryEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
    private Long taskProcessId;
    private String status;
}
