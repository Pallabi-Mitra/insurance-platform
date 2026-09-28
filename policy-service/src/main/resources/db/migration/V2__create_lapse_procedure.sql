-- ============================================================
-- V2: Stored procedure to lapse policies that were never paid
--
-- Business rule: a policy still in PENDING_PAYMENT more than
-- p_grace_days after its start date becomes LAPSED.
-- Called by a nightly batch job.
-- ============================================================

CREATE OR REPLACE PROCEDURE lapse_unpaid_policies (
    p_grace_days    IN  NUMBER,   -- IN  = value passed INTO the procedure
    p_lapsed_count  OUT NUMBER    -- OUT = value the procedure sends BACK to the caller
) AS
BEGIN
    -- ONE set-based UPDATE for all matching rows, instead of a row-by-row loop.
    -- Oracle does all the work inside the database in a single operation.
    UPDATE policy
       SET status     = 'LAPSED',
           version    = version + 1,     -- keep optimistic locking consistent with Java
           updated_at = SYSTIMESTAMP
     WHERE status     = 'PENDING_PAYMENT' -- uses idx_policy_status from V1
       AND start_date < TRUNC(SYSDATE) - p_grace_days;
       -- TRUNC(SYSDATE) = today at midnight. Minus 15 = 15 days ago.

    -- SQL%ROWCOUNT = how many rows the last statement changed.
    p_lapsed_count := SQL%ROWCOUNT;

    -- NOTE: no COMMIT here on purpose.
    -- The CALLER owns the transaction and decides when to commit.
    -- That way this procedure can be part of a bigger transaction if needed.

EXCEPTION
    -- If anything fails, re-raise the error so the caller knows and can roll back.
    -- Never silently swallow errors in a batch job: nobody would know it failed.
    WHEN OTHERS THEN
        RAISE;
END lapse_unpaid_policies;
/
