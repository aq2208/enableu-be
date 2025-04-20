package com.capstone.enableu.common.service;

import com.capstone.enableu.common.entity.BaseEntity;
import com.capstone.enableu.common.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.capstone.enableu.common.security.*;

import java.util.*;

@Service
public abstract class BaseService<E extends BaseEntity, R extends BaseRepository<E>> {

    @Autowired
    protected R repository;

    @Autowired
    private CurrentUserDetailContainer currentUserDetailsContainer;

    public CustomUserDetails getCurrentUser() {
        return this.currentUserDetailsContainer.getCurrentUser();
    }

    public E save(E entity) {
        if (entity == null) {
            return null;
        }
        preSave(entity);
        return (E) repository.save(entity);
    }

    public E saveAndFlush(E entity) {
        if (entity == null) {
            return null;
        }
        preSave(entity);

        return (E) repository.saveAndFlush(entity);
    }

    public void preSave(E entity) {
        entity.setCode(UUID.randomUUID());
        if (getCurrentUser() != null) {
            entity.setCreatedByUserId(getCurrentUser().getUserId());
            entity.setUpdatedByUserId(getCurrentUser().getUserId());
        }
        entity.setCreatedTime(new Date());
        entity.setUpdatedTime(new Date());
    }

    public boolean softDeleteList(List<E> entities) {
        if (entities == null || entities.isEmpty()) {
            return false;
        } else {
            entities.forEach(e -> {
                e.setDeleted(true);
                e.setUpdatedByUserId(getCurrentUser().getUserId());
            });
            repository.saveAll(entities);
            return true;
        }
    }

    public E findByCode(String code) {
        return findByCode(UUID.fromString(code));
    }

    public E findByCode(UUID code) {
        Object entity = repository.findByCode(code);
        if (entity == null) {
            return null;
        }
        return (E) entity;
    }

    public E findById(Long id) {
        Object entity = repository.findById(id).orElse(null);
        if (entity == null) {
            return null;
        }
        return (E) entity;
    }

    public E findByIdAndNotDeleted(Long id) {
        Object entity = repository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (entity == null) {
            return null;
        }
        return (E) entity;
    }

    public List<E> findByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        } else {
            return repository.findAllById(ids);
        }
    }

    public E update(E entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        } else {
            entity.setUpdatedTime(new Date());
            if (getCurrentUser() != null) {
                entity.setUpdatedByUserId(getCurrentUser().getUserId());
            }
            return (E) repository.save(entity);
        }
    }

    public boolean softDelete(Long id) {
        Object entityObj = repository.findByIdAndIsDeletedFalse(id).orElse(null);
        if (entityObj == null) {
            return false;
        } else {
            E entity = (E) entityObj;
            entity.setDeleted(true); // logic delete
            if (getCurrentUser() != null) {
                entity.setUpdatedByUserId(getCurrentUser().getUserId());
            }
            repository.save(entity);
            return true;
        }
    }

    public Page<E> findAll(Pageable page) {
        return repository.findAll(page);
    }
}
