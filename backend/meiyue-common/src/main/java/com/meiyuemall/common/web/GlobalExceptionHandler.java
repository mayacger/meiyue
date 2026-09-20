package com.meiyuemall.common.web;

import com.meiyuemall.common.error.BusinessException;
import com.meiyuemall.common.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一输出 {@link ApiResponse}，避免堆栈泄露给客户端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = mapStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(ex.getErrorCode().code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError == null ? ErrorCode.BAD_REQUEST.message() : fieldError.getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST.code(), msg));
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorCode.UNAUTHORIZED.code(), ErrorCode.UNAUTHORIZED.message()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorCode.FORBIDDEN.code(), ErrorCode.FORBIDDEN.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
        log.error("未处理异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("INTERNAL_ERROR", "服务器内部错误"));
    }

    private static HttpStatus mapStatus(ErrorCode code) {
        return switch (code.code()) {
            case "UNAUTHORIZED", "INVALID_CREDENTIALS" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN", "TENANT_MISMATCH", "TENANT_REQUIRED" -> HttpStatus.FORBIDDEN;
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT", "USER_EXISTS", "APPLICATION_EXISTS" -> HttpStatus.CONFLICT;
            case "BAD_REQUEST", "APPLICATION_NOT_PENDING" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
