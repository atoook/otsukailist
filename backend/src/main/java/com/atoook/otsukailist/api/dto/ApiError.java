package com.atoook.otsukailist.api.dto;

import java.time.Instant;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiError {
    private final String error; // 例: "not_found", "validation_error"
    private final String message; // 人向け
    private final Instant timestamp; // サーバ時刻
    private final Map<String, Object> details; // 任意（フィールドエラーなど）

    /**
     * Build an error payload based on a predefined error code and its default message.
     *
     * @param code error code enum
     * @return error response
     */
    public static ApiError of(ApiErrorCode code) {
        return new ApiError(
                code.code(),
                code.defaultMessage(),
                Instant.now(),
                Map.of());
    }

    /**
     * Build an error payload with a custom message while keeping the error code.
     *
     * @param code error code enum
     * @param message user-friendly message
     * @return error response
     */
    public static ApiError of(ApiErrorCode code, String message) {
        return new ApiError(
                code.code(),
                message,
                Instant.now(),
                Map.of());
    }

    /**
     * Build an error payload with additional details.
     *
     * @param code error code enum
     * @param message user-friendly message
     * @param details additional error details map
     * @return error response
     */
    public static ApiError of(ApiErrorCode code, String message, Map<String, Object> details) {
        return new ApiError(
                code.code(),
                message,
                Instant.now(),
                details);
    }
}
