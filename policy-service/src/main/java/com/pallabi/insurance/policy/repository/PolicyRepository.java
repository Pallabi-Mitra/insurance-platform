package com.pallabi.insurance.policy.repository;

import com.pallabi.insurance.policy.model.Policy;
import com.pallabi.insurance.policy.model.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY LAYER: the only place that talks to the POLICY table.
 *
 * This is an INTERFACE with no code. At startup, Spring Data JPA
 * generates a class that implements it and registers it as a bean.
 *
 * JpaRepository<Policy, Long> means:
 *   - Policy = the entity this repository manages
 *   - Long   = the type of its primary key (id)
 *
 * Methods we get for FREE:
 *   save(policy)    -> INSERT (new) or UPDATE (existing)
 *   findById(id)    -> SELECT ... WHERE id = ?
 *   findAll()       -> SELECT * FROM policy
 *   deleteById(id)  -> DELETE ... WHERE id = ?
 *   count()         -> SELECT COUNT(*) FROM policy
 *
 * @Repository also translates Oracle-specific errors into Spring's common
 * DataAccessException, so the rest of the app doesn't depend on Oracle.
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

    /**
     * DERIVED QUERY: Spring reads the method name and builds the SQL.
     *   SELECT * FROM policy WHERE policy_number = ?
     * Optional = the policy may not exist; forces the caller to handle "not found".
     */
    Optional<Policy> findByPolicyNumber(String policyNumber);

    /** SELECT * FROM policy WHERE customer_email = ?  (uses our email index) */
    List<Policy> findByCustomerEmail(String customerEmail);

    /** SELECT * FROM policy WHERE status = ?  (uses our status index; nightly batch will use it) */
    List<Policy> findByStatus(PolicyStatus status);

    /** true/false without loading the whole row. Cheaper when we only need "does it exist?" */
    boolean existsByPolicyNumber(String policyNumber);
}
