package com.pallabi.insurance.policy.exception;

/**
 * CUSTOM EXCEPTION: thrown when a policy doesn't exist.
 *
 * Why not just return null? Because null silently spreads through the code
 * and crashes somewhere far away (NullPointerException). A named exception
 * says exactly what went wrong, where. Later, the Controller layer will
 * turn this into a clean HTTP 404 "Not Found" response.
 *
 * RuntimeException = "unchecked" exception: callers aren't forced to
 * write try/catch for it. Spring's convention for business errors.
 */
public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(String policyNumber) {
        super("Policy " + policyNumber + " not found");
    }
}
