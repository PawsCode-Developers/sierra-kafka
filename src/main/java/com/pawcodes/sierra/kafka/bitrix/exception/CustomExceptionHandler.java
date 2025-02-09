package com.pawcodes.sierra.kafka.bitrix.exception;

import com.pawcodes.sierra.kafka.bitrix.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handle(final RuntimeException exception) {
        exception.printStackTrace();
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public void handleHttpClient(final HttpClientErrorException exception) {
        exception.printStackTrace();
    }

    private ResponseEntity<?> handleError(HttpStatus httpStatus, Exception exception) {
        ErrorResponse errorResponse = ErrorResponse.builder().build();
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}
