package com.pallabi.insurance.policy.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GLOBAL EXCEPTION HANDLER: one place that turns exceptions into HTTP responses.
 *
 * @RestControllerAdvice = "apply this to ALL controllers."
 * Each @ExceptionHandler method catches one kind of exception.
 *
 * Every response uses ProblemDetail (RFC 9457), the standard JSON error format:
 *   { "title": ..., "status": ..., "detail": ... }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // SLF4J logger: the standard logging API in Java. Logs go to the console now,
    // and to a log system (like CloudWatch or Grafana Loki) in production.
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 404 NOT FOUND: the policy doesn't exist.
     * The client asked for something that isn't there. Not a server failure.
     */
    @ExceptionHandler(PolicyNotFoundException.class)
    public ProblemDetail handleNotFound(PolicyNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Policy not found");
        return problem;
    }

    /**
     * 409 CONFLICT: the request is valid, but the policy's current STATE doesn't allow it.
     * Example: activating a CANCELLED policy. Thrown by Policy.activate() / cancel().
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleInvalidState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid policy state");
        return problem;
    }

    /**
     * 409 CONFLICT: OPTIMISTIC LOCKING failure.
     * Two requests tried to update the same policy at the same time; this one lost.
     * The client should reload the policy and retry. No data was overwritten.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "This policy was updated by another request. Reload and try again.");
        problem.setTitle("Concurrent update");
        return problem;
    }

    /**
     * 400 BAD REQUEST: @Valid found invalid input.
     * We return EVERY failing field with its message, so the client can fix all at once.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "One or more fields are invalid");
        problem.setTitle("Validation failed");
        problem.setProperty("errors", fieldErrors);   // extra field in the JSON
        return problem;
    }

    /**
     * 500 INTERNAL SERVER ERROR: anything we didn't expect. A REAL bug or outage.
     *
     * Two important rules:
     *  1. LOG the full error (with stack trace) so on-call engineers can debug it.
     *  2. NEVER send internal details (stack traces, SQL, table names) to the client.
     *     That leaks information attackers can use. A web security best practice.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.");
        problem.setTitle("Internal error");
        return problem;
    }
}
