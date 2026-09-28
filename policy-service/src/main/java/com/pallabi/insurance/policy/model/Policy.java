package com.pallabi.insurance.policy.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Policy ENTITY: the Java version of one row in the POLICY table.
 *
 * @Entity tells Hibernate: "this class maps to a database table."
 * @Table(name = "policy") tells it which table.
 *
 * Each field maps to one column. When we save a Policy object,
 * Hibernate generates the INSERT SQL for us. When we load one,
 * Hibernate runs a SELECT and fills these fields.
 */
@Entity
@Table(name = "policy")
public class Policy {

    /**
     * @Id = primary key.
     * @GeneratedValue(IDENTITY) = Oracle generates the number (our IDENTITY column),
     * so we never set it ourselves. It's null until the row is saved.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column links the field to the column and repeats the DB rules
    // (nullable = false means NOT NULL, unique = true means UNIQUE).
    @Column(name = "policy_number", nullable = false, unique = true, length = 30)
    private String policyNumber;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 150)
    private String customerEmail;

    /**
     * @Enumerated(STRING) stores the enum as text ("MOTOR"), not a number (0).
     * Storing numbers is dangerous: if someone reorders the enum later,
     * every existing row silently changes meaning.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 20)
    private ProductType productType;

    /**
     * Money is ALWAYS BigDecimal, never double or float.
     * Example: 0.1 + 0.2 as double = 0.30000000000000004. Unacceptable for money.
     * precision/scale match NUMBER(12,2) in the table.
     */
    @Column(name = "premium_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "coverage_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal coverageAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PolicyStatus status;

    // LocalDate = a date without time (Sept 27, 2026). Perfect for coverage dates.
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * @Version = OPTIMISTIC LOCKING.
     * Hibernate adds "WHERE version = ?" to every UPDATE and increments it.
     * If two users load version 3 and both save, the first succeeds (becomes 4),
     * the second finds no row with version 3 and fails.
     * Result: nobody's changes get silently overwritten.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // Instant = an exact moment in time (UTC). Best for audit timestamps.
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * JPA LIFECYCLE CALLBACKS
     * @PrePersist runs automatically right before the first INSERT.
     * @PreUpdate runs automatically right before every UPDATE.
     * Audit timestamps are always correct, nobody has to remember to set them.
     */
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * JPA REQUIRES a no-argument constructor: Hibernate uses it to create
     * an empty object, then fills fields from the database row.
     * "protected" so our code doesn't accidentally create half-empty policies.
     */
    protected Policy() {
    }

    /**
     * The constructor OUR code uses to create a new policy.
     * Every new policy starts as PENDING_PAYMENT: it becomes ACTIVE
     * only after the first premium is paid (a business rule).
     */
    public Policy(String policyNumber, String customerName, String customerEmail,
                  ProductType productType, BigDecimal premiumAmount, BigDecimal coverageAmount,
                  LocalDate startDate, LocalDate endDate) {
        this.policyNumber = policyNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.productType = productType;
        this.premiumAmount = premiumAmount;
        this.coverageAmount = coverageAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = PolicyStatus.PENDING_PAYMENT;
    }

    /**
     * Business methods instead of plain setters.
     * The object protects its own rules (ENCAPSULATION):
     * e.g., you can't jump a CANCELLED policy back to ACTIVE.
     */
    public void activate() {
        if (this.status != PolicyStatus.PENDING_PAYMENT && this.status != PolicyStatus.LAPSED) {
            throw new IllegalStateException("Cannot activate a policy in status " + this.status);
        }
        this.status = PolicyStatus.ACTIVE;
    }

    public void cancel() {
        if (this.status == PolicyStatus.CANCELLED || this.status == PolicyStatus.EXPIRED) {
            throw new IllegalStateException("Cannot cancel a policy in status " + this.status);
        }
        this.status = PolicyStatus.CANCELLED;
    }

    // ---- Getters: read-only access. No public setters on purpose. ----
    public Long getId() { return id; }
    public String getPolicyNumber() { return policyNumber; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public ProductType getProductType() { return productType; }
    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public BigDecimal getCoverageAmount() { return coverageAmount; }
    public PolicyStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
