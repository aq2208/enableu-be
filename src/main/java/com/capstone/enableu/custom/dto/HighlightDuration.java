package com.capstone.enableu.custom.dto;
import com.capstone.enableu.custom.util.Validate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class HighlightDuration {
    String startTime;
    String endTime;
}

