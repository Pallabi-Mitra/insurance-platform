package com.pallabi.insurance.policy.service;

import com.pallabi.insurance.policy.exception.PolicyNotFoundException;
import com.pallabi.insurance.policy.model.Policy;
import com.pallabi.insurance.policy.model.ProductType;
import com.pallabi.insurance.policy.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * SERVICE LAYER: business rules for policies.
 *
 * @Service marks this as a Spring bean. Component scan finds it,
 * Spring creates ONE instance (a "singleton") and injects it wherever needed.
 */
@Service
public class PolicyService {

    // Yearly premium rates by product. BigDecimal, because this is money math.
    private static final BigDecimal MOTOR_RATE = new BigDecimal("0.03");   // 3% of coverage
    private static final BigDecimal HEALTH_RATE = new BigDecimal("0.05");  // 5%
    private static final BigDecimal LIFE_RATE = new BigDecimal("0.01");    // 1%

    // "final" = set once in the constructor, never changed. Safe and clear.
    private final PolicyRepository policyRepository;

    /**
     * CONSTRUCTOR INJECTION (the recommended way to do Dependency Injection).
     *
     * We never write "new PolicyRepository()". Spring sees this constructor needs
     * a PolicyRepository, finds the one it generated, and passes it in.
     *
     * Why constructor injection instead of @Autowired on a field?
     *  1. Dependencies are obvious: just read the constructor.
     *  2. The field can be final (can't be accidentally replaced).
     *  3. Easy to unit test: pass a fake (mock) repository into the constructor.
     */
    public PolicyService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * Creates a new policy.
     *
     * @Transactional = everything in this method happens in ONE database transaction.
     * If anything throws an exception, ALL database changes are rolled back.
     * Either the whole thing succeeds, or nothing is saved (ATOMICITY, the "A" in ACID).
     */
    @Transactional
    public Policy createPolicy(String customerName, String customerEmail,
                               ProductType productType, BigDecimal coverageAmount,
                               LocalDate startDate) {

        BigDecimal premium = calculatePremium(productType, coverageAmount);
        LocalDate endDate = startDate.plusYears(1);   // every policy lasts 1 year
        String policyNumber = generatePolicyNumber();

        Policy policy = new Policy(policyNumber, customerName, customerEmail,
                productType, premium, coverageAmount, startDate, endDate);

        // save() -> Hibernate runs INSERT. Oracle fills the id, @PrePersist fills timestamps.
        return policyRepository.save(policy);
    }

    /**
     * readOnly = true tells Spring and Hibernate "this method only reads".
     * Hibernate skips change tracking, which makes reads a bit faster.
     *
     * orElseThrow: if the Optional is empty, throw our named exception.
     */
    @Transactional(readOnly = true)
    public Policy getByPolicyNumber(String policyNumber) {
        return policyRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new PolicyNotFoundException(policyNumber));
    }

    @Transactional(readOnly = true)
    public List<Policy> getByCustomerEmail(String customerEmail) {
        return policyRepository.findByCustomerEmail(customerEmail);
    }

    /**
     * Activates a policy (later, Billing Service will trigger this after payment).
     *
     * Notice: NO save() call here. This is DIRTY CHECKING.
     * Inside a transaction, Hibernate remembers what the loaded object looked like.
     * When the transaction commits, it compares, sees status changed,
     * and automatically runs the UPDATE. @Version also increments here.
     */
    @Transactional
    public Policy activatePolicy(String policyNumber) {
        Policy policy = getByPolicyNumber(policyNumber);
        policy.activate();   // the entity enforces the rule (can't activate a CANCELLED policy)
        return policy;
    }

    @Transactional
    public Policy cancelPolicy(String policyNumber) {
        Policy policy = getByPolicyNumber(policyNumber);
        policy.cancel();
        return policy;
    }

    /**
     * Premium = coverage x yearly rate, rounded to 2 decimals.
     *
     * "switch" expression (modern Java): returns a value directly.
     * If someone adds a new ProductType and forgets it here, the code won't compile.
     *
     * HALF_UP rounding = normal school rounding (2.345 -> 2.35). Always state
     * rounding explicitly for money; never leave it to defaults.
     */
    BigDecimal calculatePremium(ProductType productType, BigDecimal coverageAmount) {
        BigDecimal rate = switch (productType) {
            case MOTOR -> MOTOR_RATE;
            case HEALTH -> HEALTH_RATE;
            case LIFE -> LIFE_RATE;
        };
        return coverageAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Format: POL-<year>-<8 random characters>, e.g. POL-2026-7F3A9C21.
     * UUID = a random ID that's practically never repeated.
     * If it ever did repeat, the UNIQUE constraint in Oracle would reject it.
     */
    private String generatePolicyNumber() {
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "POL-" + Year.now().getValue() + "-" + random;
    }
}
