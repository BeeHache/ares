package net.blackhacker.ares.controller;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ControllerException extends RuntimeException {
    final private HttpStatus status;
    final private ErrorResponse errorResponse;

    public ControllerException(HttpStatus status, String message) {
        this.status = status;
        this.errorResponse = new ErrorResponse(status.value(), message, LocalDateTime.now());
    }
}
