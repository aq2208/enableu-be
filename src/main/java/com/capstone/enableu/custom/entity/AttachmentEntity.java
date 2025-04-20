package com.capstone.enableu.custom.entity;

import com.capstone.enableu.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "attachment")
public class AttachmentEntity extends BaseEntity {
    private String attachment_name;
    private String attachment_id;
    private String transcript;
}
