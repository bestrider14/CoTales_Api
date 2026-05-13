package com.cotales.cotales_api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cotales.cotales_api.common.exception.ConflictException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void handleConflict_returns409WithMessage() {
        ConflictException ex = new ConflictException("Email already in use");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("Email already in use");
        assertThat(response.getBody().fieldErrors()).isNull();
    }

    @Test
    void handleResponseStatus_mapsStatusAndMessage() {
        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("Email already in use");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/test");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void handleResponseStatus_doesNotIncludeFieldErrors() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex, request);

        assertThat(response.getBody().fieldErrors()).isNull();
    }

    @Test
    void handleValidation_returns400WithFieldErrors() {
        FieldError fieldError =
                new FieldError("createUserRequest", "email", "must be a well-formed email address");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("email");
        assertThat(response.getBody().fieldErrors().get(0).message())
                .isEqualTo("must be a well-formed email address");
    }

    @Test
    void handleValidation_returnsAllFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors())
                .thenReturn(
                        List.of(
                                new FieldError("req", "email", "invalid email"),
                                new FieldError("req", "password", "too short")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getBody().fieldErrors()).hasSize(2);
    }

    @Test
    void handleUnreadableMessage_returns400WithMalformedJsonMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnreadableMessage(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Malformed JSON body");
    }

    @Test
    void handleException_returns500() {
        ResponseEntity<ErrorResponse> response = handler.handleException(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().fieldErrors()).isNull();
    }
}
