package com.songplayer.api.error;

import com.songplayer.application.InvalidPlaylistOrderException;
import com.songplayer.application.PlayerStateException;
import com.songplayer.application.ResourceNotFoundException;
import com.songplayer.application.UnsupportedExportFormatException;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converts domain and infrastructure exceptions into the public API error contract. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(Instant.now(), HttpStatus.NOT_FOUND.value(), "RESOURCE_NOT_FOUND", exception.getMessage(), List.of()));
    }

    @ExceptionHandler(InvalidPlaylistOrderException.class)
    ResponseEntity<ApiError> handleInvalidOrder(InvalidPlaylistOrderException exception) {
        return ResponseEntity.badRequest().body(error("INVALID_PLAYLIST_ORDER", exception.getMessage(), List.of()));
    }

    @ExceptionHandler(UnsupportedExportFormatException.class)
    ResponseEntity<ApiError> handleUnsupportedFormat(UnsupportedExportFormatException exception) {
        return ResponseEntity.badRequest().body(error("UNSUPPORTED_EXPORT_FORMAT", exception.getMessage(), List.of()));
    }

    @ExceptionHandler(PlayerStateException.class)
    ResponseEntity<ApiError> handlePlayerState(PlayerStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(Instant.now(), HttpStatus.CONFLICT.value(), "INVALID_PLAYER_STATE", exception.getMessage(), List.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation: {}", exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(Instant.now(), HttpStatus.CONFLICT.value(), "DATA_CONFLICT",
                        "The request conflicts with existing data.", List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleInvalidRequest(MethodArgumentNotValidException exception) {
        List<ApiError.FieldViolation> violations = exception.getBindingResult().getFieldErrors().stream()
                .map(err -> new ApiError.FieldViolation(err.getField(), err.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", "Request validation failed", violations));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        List<ApiError.FieldViolation> violations = exception.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldViolation(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", "Request validation failed", violations));
    }

    /** Catch-all: prevents stack traces from leaking to the client on unexpected errors. */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_ERROR", "An unexpected error occurred.", List.of()));
    }

    private ApiError error(String code, String message, List<ApiError.FieldViolation> violations) {
        return new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), code, message, violations);
    }
}
