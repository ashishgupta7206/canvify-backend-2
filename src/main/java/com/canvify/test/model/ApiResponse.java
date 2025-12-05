package com.canvify.test.model;

import com.canvify.test.model.Pagination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> errors;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Pagination pagination;

    private static final String DEFAULT_SUCCESS_MESSAGE = "Operation completed successfully.";

    private ApiResponse(boolean success, String message, T data, Map<String, Object> errors, Pagination pagination) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.pagination = pagination;
    }

    // Success Response
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, DEFAULT_SUCCESS_MESSAGE, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, null);
    }

    // Success with Pagination
    public static <T> ApiResponse<T> success(T data, String message, Pagination pagination) {
        return new ApiResponse<>(true, message, data, null, pagination);
    }

    // Error Response
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null, null);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, Object> errors) {
        return new ApiResponse<>(false, message, null, errors, null);
    }
}
