package com.atoook.otsukailist.api.advice;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.atoook.otsukailist.api.dto.ApiError;
import com.atoook.otsukailist.api.dto.ApiErrorCode;
import com.atoook.otsukailist.exception.ResourceNotFoundException;

@RestControllerAdvice(basePackages = "com.atoook.otsukailist.api.controller")
public class ApiExceptionHandler {

    /**
     * Handle resource-not-found errors for API requests.
     *
     * @param e thrown exception
     * @return 404 response body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(ApiErrorCode.NOT_FOUND, e.getMessage()));
    }

    /**
     * Handle generic bad request scenarios such as invalid arguments.
     *
     * @param e thrown exception
     * @return 400 response body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ApiErrorCode.BAD_REQUEST, e.getMessage()));
    }

    /**
     * Handle validation errors from bean validation.
     *
     * @param e binding exception
     * @return 400 response body with field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().forEach(err -> {
            // 同じフィールドで複数エラーが来たら先勝ち（好みで変えてOK）
            fieldErrors.putIfAbsent(err.getField(), err.getDefaultMessage());
        });

        Map<String, Object> details = Map.of("fieldErrors", fieldErrors);

        String msg = fieldErrors.isEmpty()
                ? ApiErrorCode.VALIDATION_ERROR.defaultMessage()
                : "入力内容に誤りがあります";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ApiErrorCode.VALIDATION_ERROR, msg, details));
    }

    /**
     * Handle constraint violations raised by the database.
     *
     * @param e data integrity violation
     * @return 409 response body
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException e) {
        // ここは “とりあえず実務的に” 分かりやすいメッセージに寄せる
        // member の UNIQUE(list_id, display_name) が最頻出想定
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(
                        ApiErrorCode.CONFLICT,
                        "制約違反が発生しました（同一リスト内で重複がないか確認してください）"));
    }

    /**
     * Handle unexpected exceptions and respond with a generic error.
     *
     * @param e unexpected exception
     * @return 500 response with generic error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInternal(Exception e) {
        // ログは別途（ここではレスポンスだけ）
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(ApiErrorCode.INTERNAL_ERROR));
    }

}
