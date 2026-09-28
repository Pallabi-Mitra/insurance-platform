package com.pallabi.insurance.policy.service;

import com.pallabi.insurance.policy.exception.PolicyNotFoundException;
import com.pallabi.insurance.policy.model.Policy;
import com.pallabi.insurance.policy.model.PolicyStatus;
import com.pallabi.insurance.policy.model.ProductType;
import com.pallabi.insurance.policy.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTS for PolicyService, with a MOCKED repository.
 *
 * @ExtendWith(MockitoExtension.class) turns on Mockito for this test class.
 * @Mock creates a FAKE PolicyRepository. It does nothing unless we tell it to.
 *
 * Why mock? We're testing the SERVICE's logic (premium math, rules, errors),
 * not the database. No Oracle needed, tests run in milliseconds.
 */
@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;   // the fake

    private PolicyService policyService;          // the REAL class we're testing

    /**
     * @BeforeEach runs before EVERY test: a fresh service each time.
     * This is where constructor injection pays off: we just pass the fake in.
     */
    @BeforeEach
    void setUp() {
        policyService = new PolicyService(policyRepository);
    }

    @Test
    @DisplayName("Motor premium is 3% of coverage")
    void motorPremiumIsThreePercent() {
        BigDecimal premium = policyService.calculatePremium(ProductType.MOTOR, new BigDecimal("50000"));

        // Always compare BigDecimal with isEqualByComparingTo:
        // 1500 and 1500.00 are the same amount but not "equal" objects.
        assertThat(premium).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("Health premium is 5% and Life premium is 1%")
    void healthAndLifePremiums() {
        assertThat(policyService.calculatePremium(ProductType.HEALTH, new BigDecimal("20000")))
                .isEqualByComparingTo("1000.00");
        assertThat(policyService.calculatePremium(ProductType.LIFE, new BigDecimal("100000")))
                .isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Creating a policy calculates premium, sets 1 year term, and saves it")
    void createPolicySavesCorrectPolicy() {
        // Arrange: tell the fake "when save() is called, return whatever you were given."
        when(policyRepository.save(any(Policy.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        Policy created = policyService.createPolicy("Pallabi", "p@example.com",
                ProductType.MOTOR, new BigDecimal("50000"), LocalDate.of(2026, 10, 1));

        // Assert: ArgumentCaptor grabs the exact object passed to save(), so we can inspect it.
        ArgumentCaptor<Policy> captor = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepository).save(captor.capture());   // verify = "was save() called once?"
        Policy saved = captor.getValue();

        assertThat(saved.getPremiumAmount()).isEqualByComparingTo("1500.00");
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(saved.getStatus()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        assertThat(saved.getPolicyNumber()).startsWith("POL-");
        assertThat(created).isSameAs(saved);
    }

    @Test
    @DisplayName("Getting a missing policy throws PolicyNotFoundException")
    void missingPolicyThrowsNotFound() {
        // Arrange: the fake returns "nothing found"
        when(policyRepository.findByPolicyNumber("POL-FAKE")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(PolicyNotFoundException.class,
                () -> policyService.getByPolicyNumber("POL-FAKE"));
    }

    @Test
    @DisplayName("Activating a pending policy makes it ACTIVE")
    void activatePendingPolicy() {
        // Arrange
        Policy pending = new Policy("POL-1", "A", "a@example.com", ProductType.LIFE,
                new BigDecimal("100.00"), new BigDecimal("10000.00"),
                LocalDate.of(2026, 10, 1), LocalDate.of(2027, 10, 1));
        when(policyRepository.findByPolicyNumber("POL-1")).thenReturn(Optional.of(pending));

        // Act
        Policy result = policyService.activatePolicy("POL-1");

        // Assert
        assertThat(result.getStatus()).isEqualTo(PolicyStatus.ACTIVE);
        // No save() expected: dirty checking handles the update in a real transaction.
        verify(policyRepository, never()).save(any());
    }
}
