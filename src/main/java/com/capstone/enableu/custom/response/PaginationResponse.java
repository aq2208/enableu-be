package com.capstone.enableu.custom.response;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static graphql.com.google.common.primitives.Ints.max;
import static java.lang.Math.min;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class PaginationResponse<T> {

    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    @Builder
    private static class Pagination {
        private Integer page;
        private Integer size;
        private Integer totalPage;
    }
    private Pagination pagination = new Pagination(0, 0, 0);
    private List<T> data;

    public static <R> PaginationResponse<R> of(Page<R> pageData) {
        return PaginationResponse
                .<R>builder()
                .pagination(Pagination.builder()
                        .page(pageData.getPageable().getPageNumber()+1)
                        .size(pageData.getPageable().getPageSize())
                        .totalPage(pageData.getTotalPages())
                        .build())
                .data(pageData.getContent())
                .build();
    }

    public static <R> PaginationResponse<R> of(List<R> data, Pageable pageable) {
        Page<R> pageData = new PageImpl<>(data, pageable, data.size());
        PaginationResponse<R> paginationResponse = new PaginationResponse<>();
        if (pageable.getPageNumber() <= 0 || ((pageable.getPageNumber()-1)*pageable.getPageSize() > data.size() )) {
            paginationResponse.setPagination(new Pagination(0, 0, 0));
            if (pageable.getPageNumber() == 1) {
                paginationResponse.setPagination(new Pagination(1, pageable.getPageSize(), 1));
                paginationResponse.setData(data);
            }
            return paginationResponse;
        }
        int totalPages = (data.size() + pageable.getPageSize() - 1) / pageable.getPageSize();
        paginationResponse.setPagination(new Pagination(pageable.getPageNumber(), pageable.getPageSize(), totalPages));
        paginationResponse.setData(data.subList((pageData.getNumber()-1)*pageable.getPageSize(), min(pageData.getNumber()*pageable.getPageSize(), data.size())));

        return paginationResponse;
    }
}
