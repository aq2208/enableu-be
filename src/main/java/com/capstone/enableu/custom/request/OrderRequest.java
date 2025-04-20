package com.capstone.enableu.custom.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    Integer moduleId;
    List<Integer> taskIds;
}
