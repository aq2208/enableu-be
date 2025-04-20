package com.capstone.enableu.custom.entity;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.custom.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "user_task")
public class UserTaskEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    @ManyToOne
    @JoinColumn(name = "task_id")
    private TaskEntity task;
    private String status;
}
