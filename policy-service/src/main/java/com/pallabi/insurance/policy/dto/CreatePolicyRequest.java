package com.pallabi.insurance.policy.dto;

import com.pallabi.insurance.policy.model.ProductType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * REQUEST DTO: exactly what a client is ALLOWED to send when creating a policy.
 *
 * "record" (modern Java) = an immutable data class. Java auto-generates the
 * constructor, getters, equals, hashCode, toString. Perfect for DTOs.
 *
 * Notice what's NOT here: id, policyNumber, premium, status, version.
 * The client can't set those. The SERVER decides them. That's a security rule.
 *
 * The annotations are BEAN VALIDATION rules. When the controller uses @Valid,
 * Spring checks them BEFORE our code runs. Bad input gets rejected with HTTP 400.
 */
public record CreatePolicyRequest(

        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Customer name must be at most 100 characters")
        String customerName,

        @NotBlank(message = "Customer email is required")
        @Email(message = "Customer email must be a valid email address")
        @Size(max = 150)
        String customerEmail,

        @NotNull(message = "Product type is required (MOTOR, HEALTH, or LIFE)")
        ProductType productType,

        @NotNull(message = "Coverage amount is required")
        @DecimalMin(value = "1000.00", message = "Coverage must be at least 1000.00")
        @DecimalMax(value = "10000000.00", message = "Coverage must be at most 10,000,000.00")
        BigDecimal coverageAmount,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        LocalDate startDate
) {
}
