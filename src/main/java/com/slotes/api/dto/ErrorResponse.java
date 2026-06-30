package com.slotes.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error
) {
    public static ErrorResponse of(int status, String error) {
        return new ErrorResponse(LocalDateTime.now(), status, error);
    }
}
