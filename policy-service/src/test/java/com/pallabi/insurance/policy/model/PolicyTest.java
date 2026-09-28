package com.pallabi.insurance.policy.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * UNIT TESTS for the Policy entity's business rules.
 *
 * No Spring, no database, no mocks: just plain Java objects.
 * The simplest and fastest kind of test.
 *
 * @Test marks a method as a test. JUnit finds and runs every @Test method.
 * @DisplayName gives it a readable name in the test report.
 */
class PolicyTest {

    // Helper: builds a fresh policy for each test, so tests don't affect each other.
    private Policy newPolicy() {
        return new Policy("POL-TEST-1", "Test User", "test@example.com",
                ProductType.MOTOR, new BigDecimal("1500.00"), new BigDecimal("50000.00"),
                LocalDate.of(2026, 10, 1), LocalDate.of(2027, 10, 1));
    }

    @Test
    @DisplayName("A new policy starts as PENDING_PAYMENT")
    void newPolicyStartsPendingPayment() {
        // Arrange + Act
        Policy policy = newPolicy();

        // Assert: AssertJ's assertThat reads like English
        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("A pending policy can be activated")
    void pendingPolicyCanBeActivated() {
        // Arrange
        Policy policy = newPolicy();

        // Act
        policy.activate();

        // Assert
        assertThat(policy.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
    }

    @Test
    @DisplayName("A cancelled policy cannot be activated")
    void cancelledPolicyCannotBeActivated() {
        // Arrange
        Policy policy = newPolicy();
        policy.cancel();

        // Act + Assert: assertThrows checks that the code throws the expected exception.
        // Testing the "unhappy path" matters as much as the happy path.
        IllegalStateException ex = assertThrows(IllegalStateException.class, policy::activate);
        assertThat(ex.getMessage()).contains("CANCELLED");
    }

    @Test
    @DisplayName("A cancelled policy cannot be cancelled again")
    void cancelledPolicyCannotBeCancelledAgain() {
        Policy policy = newPolicy();
        policy.cancel();

        assertThrows(IllegalStateException.class, policy::cancel);
    }
}
