package com.capstone.enableu.custom.request;

import lombok.Data;

@Data
public class SearchAllRequest {
    private String search;
    private Integer pageNumber;
    private Integer pageSize;
}
