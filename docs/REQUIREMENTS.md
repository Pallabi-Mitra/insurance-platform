# Insurance Platform: Requirements

## Goal
A backend platform where customers buy insurance policies, pay premiums, and file claims.
Built to be reliable, auditable, and safe with money.

## Users
- Customer: gets quotes, buys policies, pays premiums, files claims
- Claims adjuster: reviews and approves or rejects claims
- Admin: manages products and views reports

## Functional requirements
1. Products: support Motor, Health, and Life insurance, each with pricing rules
2. Quotes: customer gets a premium quote based on product and inputs
3. Policies: a quote converts to a policy with lifecycle ACTIVE, LAPSED, CANCELLED, EXPIRED
4. Billing: premiums paid by card (Stripe test mode), confirmed only via verified webhook
5. Claims: customer files a claim; adjuster approves or rejects; approved claims trigger a payout
6. Ledger: every premium and payout recorded as a double-entry transaction
7. Notifications: customer notified on policy issue, payment, claim decision
8. Nightly batch: lapse unpaid policies, send renewal reminders

## Non-functional requirements
- No duplicate payments or payouts, even on retries (idempotency)
- No data loss between database and Kafka (transactional outbox)
- Full audit trail on every policy and claim change
- Secured APIs (JWT authentication, role-based access)
- Automated tests and CI on every pull request
- Health checks and metrics for production support

## Out of scope (for now)
- Real payment processing, real customer PII, mobile app
