// Developed by: Mohammad Sheikh Qasem
package com.huffman.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex){

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "status",  "400 Bad Request",
                        "error",   "Invalid input",
                        "message", ex.getMessage()
                ));
    }


    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(java.util.NoSuchElementException ex){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status",  "404 Not Found",
                        "error",   "Element not found",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex){

        return ResponseEntity.internalServerError()
                .body(Map.of(
                        "status",  "500 Internal Server Error",
                        "error",   "Unexpected error",
                        "message", ex.getMessage() != null ? ex.getMessage() : "Unknown error"
                ));
    }
}


/**
 * ============================================================
 * GlobalExceptionHandler.java
 * ------------------------------------------------------------
 * Spring @RestControllerAdvice that intercepts exceptions thrown
 * anywhere in the application and formats them as clean JSON
 * error responses instead of the default Spring error page.
 *
 * Mapped exceptions:
 *   IllegalArgumentException → 400 Bad Request
 *   NoSuchElementException   → 404 Not Found
 *   Exception (catch-all)    → 500 Internal Server Error
 * ============================================================
 */
/**
 * Handles validation errors from service / model layer.
 * Returns HTTP 400 with the exception message.
 */

/**
 * Handles cases where a required element cannot be found.
 * Returns HTTP 404.
 */

/**
 * Generic catch-all for unexpected errors.
 * Returns HTTP 500 with a sanitised message.
 */
