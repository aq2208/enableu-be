package com.capstone.enableu.custom.dto;

import com.capstone.enableu.custom.response.TaskResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class StepInput {
    String name;
    String content;
    String text;
    Integer orderId;
    String attachmentUrl;
}
