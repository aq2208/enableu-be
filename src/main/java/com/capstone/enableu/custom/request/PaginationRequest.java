package com.capstone.enableu.custom.request;

import lombok.*;
import org.springframework.data.domain.Pageable;


@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class PaginationRequest<T> {
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    @Builder
    private static class Pagination {
        private Integer page;
        private Integer size;
    }
    private Pagination pagination;
    private T input;

    public static <R> PaginationRequest<R> of(Pageable pageData) {
        return PaginationRequest .<R>builder()
                .pagination(Pagination.builder().
                        page(pageData.getPageNumber()).
                        size(pageData.getPageSize()).build()).
                        build();

    }
}