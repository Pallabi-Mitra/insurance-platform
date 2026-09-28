-- ============================================================
-- V1: Create the POLICY table
-- Flyway runs this file once and records it in the
-- flyway_schema_history table so it never runs again.
-- ============================================================

CREATE TABLE policy (
    -- Primary key. "GENERATED ... AS IDENTITY" makes Oracle auto-number each new row (1, 2, 3...).
                        id               NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- Business identifier shown to customers, e.g. "POL-2026-000001".
    -- UNIQUE = the database itself refuses duplicates, even if the Java code has a bug.
                        policy_number    VARCHAR2(30)   NOT NULL UNIQUE,

                        customer_name    VARCHAR2(100)  NOT NULL,
                        customer_email   VARCHAR2(150)  NOT NULL,

    -- CHECK constraint = only these values are allowed. Protects data at the lowest level.
                        product_type     VARCHAR2(20)   NOT NULL
                     CHECK (product_type IN ('MOTOR', 'HEALTH', 'LIFE')),

    -- Money is NUMBER(12,2): up to 12 digits, exactly 2 after the decimal.
    -- Never use floating point types for money, they cause rounding errors.
                        premium_amount   NUMBER(12,2)   NOT NULL CHECK (premium_amount > 0),
                        coverage_amount  NUMBER(14,2)   NOT NULL CHECK (coverage_amount > 0),

    -- Policy lifecycle state.
                        status           VARCHAR2(20)   NOT NULL
                     CHECK (status IN ('PENDING_PAYMENT', 'ACTIVE', 'LAPSED', 'CANCELLED', 'EXPIRED')),

                        start_date       DATE           NOT NULL,
                        end_date         DATE           NOT NULL,

    -- Optimistic locking: increases by 1 on every update.
    -- If two people edit the same policy at once, the second save fails instead of silently overwriting.
                        version          NUMBER         DEFAULT 0 NOT NULL,

    -- Audit columns: when was this row created and last changed.
                        created_at       TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
                        updated_at       TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,

    -- Business rule enforced by the database: a policy can't end before it starts.
                        CONSTRAINT chk_policy_dates CHECK (end_date > start_date)
);

-- Index on status: the nightly batch will search "all policies WHERE status = 'PENDING_PAYMENT'".
-- Without an index Oracle scans every row (full table scan); with it, Oracle jumps straight to matches.
CREATE INDEX idx_policy_status ON policy (status);

-- Index on email: customers look up their own policies by email.
CREATE INDEX idx_policy_customer_email ON policy (customer_email);