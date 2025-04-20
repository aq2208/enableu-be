package com.capstone.enableu.custom.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoiceSearchResponse {
    String transcript;
    String message;
}
