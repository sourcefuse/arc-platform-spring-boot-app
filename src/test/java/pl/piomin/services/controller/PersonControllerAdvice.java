package pl.piomin.services.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Person API.
 * Provides centralized exception handling across all controllers.
 */
@ControllerAdvice
public class PersonControllerAdvice {

    /**
     * Handles validation exceptions and returns appropriate error responses.
     *
     * @param ex The constraint violation exception
     * @param request The web request
     * @return A response entity with error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });
        
        body.put("details", errors);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles general exceptions and returns appropriate error responses.
     *
     * @param ex The exception
     * @param request The web request
     * @return A response entity with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
