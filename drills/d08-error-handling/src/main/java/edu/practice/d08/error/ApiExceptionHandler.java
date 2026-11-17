package edu.practice.d08.error;

import edu.practice.d08.dto.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Catches exceptions thrown by ANY controller (and the services they call)
 * and turns them into an HTTP status + an ApiError JSON body.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  // TODO d08-1: IllegalArgumentException   -> HTTP 400, body new ApiError("BAD_REQUEST", <the exception's message>)
  // TODO d08-2: TicketNotFoundException    -> HTTP 404, body new ApiError("NOT_FOUND", <the exception's message>)
  // TODO d08-3: TicketConflictException    -> HTTP 409, body new ApiError("CONFLICT", <the exception's message>)
  //
  // One public method per exception. Each one:
  //   - is annotated @ExceptionHandler(TheException.class)
  //   - takes the exception as its parameter
  //   - returns ResponseEntity<ApiError>

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> badRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("BAD_REQUEST", e.getMessage()));
  }

  @ExceptionHandler(TicketNotFoundException.class)
  public ResponseEntity<ApiError> notFound(TicketNotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("NOT_FOUND", e.getMessage()));
  }

  @ExceptionHandler(TicketConflictException.class)
  public ResponseEntity<ApiError> conflict(TicketConflictException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("CONFLICT", e.getMessage()));
  }
}
