package com.cotales.cotales_api.common;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        int status = ex.getStatusCode().value();
        String error =
                HttpStatus.resolve(status) != null
                        ? HttpStatus.resolve(status).getReasonPhrase()
                        : "Error";
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, error, ex.getReason(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldError> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                fe ->
                                        new ErrorResponse.FieldError(
                                                fe.getField(), fe.getDefaultMessage()))
                        .toList();
        return ResponseEntity.badRequest()
                .body(
                        new ErrorResponse(
                                400,
                                "Bad Request",
                                "Validation failed",
                                request.getRequestURI(),
                                OffsetDateTime.now(),
                                fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(
                        ErrorResponse.of(
                                400,
                                "Bad Request",
                                "Malformed JSON body",
                                request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request) {
        return ResponseEntity.internalServerError()
                .body(
                        ErrorResponse.of(
                                500,
                                "Internal Server Error",
                                "An unexpected error occurred",
                                request.getRequestURI()));
    }
}
