package com.capstone.enableu.custom.dto;

import com.capstone.enableu.custom.enums.Status;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryFilter {
    private List<Status> status;
    private String createdBy;
}