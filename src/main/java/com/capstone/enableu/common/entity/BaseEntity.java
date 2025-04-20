package com.capstone.enableu.common.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@MappedSuperclass
@Data
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected UUID code;

    protected Date createdTime;

    protected Date updatedTime;

    protected Long createdByUserId;

    protected Long updatedByUserId;

    protected boolean isDeleted;

}

