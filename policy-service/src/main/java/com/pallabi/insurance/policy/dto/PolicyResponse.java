package com.pallabi.insurance.policy.dto;

import com.pallabi.insurance.policy.model.Policy;
import com.pallabi.insurance.policy.model.PolicyStatus;
import com.pallabi.insurance.policy.model.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * RESPONSE DTO: exactly what we SEND BACK to the client.
 *
 * We deliberately leave out internal fields like the database id and version.
 * Clients use policyNumber (the business id). The database id is an internal detail,
 * and exposing sequential ids lets attackers guess other records (1, 2, 3...).
 *
 * Jackson (Spring's JSON library) turns this record into JSON automatically.
 */
public record PolicyResponse(
        String policyNumber,
        String customerName,
        String customerEmail,
        ProductType productType,
        BigDecimal premiumAmount,
        BigDecimal coverageAmount,
        PolicyStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt
) {

    /**
     * MAPPER: converts an Entity into a Response DTO.
     * "static factory method": call it as PolicyResponse.from(policy).
     */
    public static PolicyResponse from(Policy policy) {
        return new PolicyResponse(
                policy.getPolicyNumber(),
                policy.getCustomerName(),
                policy.getCustomerEmail(),
                policy.getProductType(),
                policy.getPremiumAmount(),
                policy.getCoverageAmount(),
                policy.getStatus(),
                policy.getStartDate(),
                policy.getEndDate(),
                policy.getCreatedAt()
        );
    }
}
