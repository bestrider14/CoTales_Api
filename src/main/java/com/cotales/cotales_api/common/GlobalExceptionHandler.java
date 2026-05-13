package com.cotales.cotales_api.common;

import com.cotales.cotales_api.common.exception.ConflictException;
import com.cotales.cotales_api.common.exception.NotFoundException;
import com.cotales.cotales_api.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.of(
                                401, "Unauthorized", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingCookie(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.of(
                                401,
                                "Unauthorized",
                                "Refresh token cookie missing",
                                request.getRequestURI()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        ErrorResponse.of(
                                401,
                                "Unauthorized",
                                "Invalid email or password",
                                request.getRequestURI()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict", ex.getMessage(), request.getRequestURI()));
    }

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
