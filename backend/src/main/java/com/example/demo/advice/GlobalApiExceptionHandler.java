package com.example.demo.advice;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 统一返回带 {@code message} 的 JSON，便于前台与排查（默认 /error 往往不含 message）。
 */
@RestControllerAdvice
public class GlobalApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String reason = ex.getReason();
        if (reason == null || reason.isEmpty()) {
            reason = status.getReasonPhrase();
        }
        return ResponseEntity.status(status).body(jsonError(status.value(), status.getReasonPhrase(), reason));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        msg = truncate(msg);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonError(500, "Internal Server Error", msg != null ? msg : ex.getClass().getSimpleName()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        String msg = truncate(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(jsonError(500, "Internal Server Error", msg != null ? msg : ex.getClass().getSimpleName()));
    }

    private static String truncate(String msg) {
        if (msg == null) {
            return null;
        }
        if (msg.length() > 600) {
            return msg.substring(0, 600) + "…";
        }
        return msg;
    }

    private static Map<String, Object> jsonError(int status, String error, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status);
        m.put("error", error);
        m.put("message", message);
        return m;
    }
}
