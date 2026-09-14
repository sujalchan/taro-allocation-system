package nz.ac.aut.comp713.customer_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.aut.comp713.customer_service.dto.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(CustomerNotFoundException.class)
        public ResponseEntity<ApiError> handleCustomerNotFound(
                        CustomerNotFoundException exception,
                        HttpServletRequest request) {

                ApiError error = new ApiError(
                                "CUSTOMER_NOT_FOUND",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        @ExceptionHandler(CustomerAlreadyExistsException.class)
        public ResponseEntity<ApiError> handleCustomerAlreadyExists(
                        CustomerAlreadyExistsException exception,
                        HttpServletRequest request) {

                ApiError error = new ApiError(
                                "CUSTOMER_ALREADY_EXISTS",
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiError> handleValidationError(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                String message = exception
                                .getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Validation failed");

                ApiError error = new ApiError(
                                "VALIDATION_ERROR",
                                message,
                                request.getRequestURI());

                return ResponseEntity
                                .badRequest()
                                .body(error);
        }
}