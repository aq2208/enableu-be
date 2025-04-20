package com.capstone.enableu.common.repository;

import com.capstone.enableu.common.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<E extends BaseEntity> extends PagingAndSortingRepository<E, Long>, JpaRepository<E, Long>, JpaSpecificationExecutor<E> {

    E findByCode(UUID code);

    Page<E> findByIsDeleted(Pageable page, boolean isDeleted);

    List<E> findByIsDeletedFalse();

    Optional<E> findByIdAndIsDeletedFalse(Long id);

}
