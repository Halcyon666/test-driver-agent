package com.example.testdriver.service;

import com.example.testdriver.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests written FIRST from specs/example-money-transfer.md
 * Each test maps to a specific acceptance criterion or edge case.
 */
@ExtendWith(MockitoExtension.class)
class MoneyTransferServiceTest {

    @Mock
    private MoneyAuditPort moneyAuditPort;

    @InjectMocks
    private MoneyTransferService transferService;

    @Nested
    @DisplayName("Acceptance Criteria")
    class AcceptanceCriteria {

        @Test
        @DisplayName("AC-1: transfers between same currency accounts")
        void shouldTransferBetweenSameCurrency() {
            Money source = new Money(new BigDecimal("100.00"), "USD");
            Money destination = new Money(new BigDecimal("50.00"), "USD");

            TransferResult result = transferService.transfer(source, destination, new BigDecimal("30.00"));

            assertThat(result.newSource()).isEqualTo(new Money(new BigDecimal("70.00"), "USD"));
            assertThat(result.newDestination()).isEqualTo(new Money(new BigDecimal("80.00"), "USD"));
            verify(moneyAuditPort).recordOperation(
                    org.mockito.ArgumentMatchers.eq("TRANSFER"),
                    org.mockito.ArgumentMatchers.eq(source),
                    org.mockito.ArgumentMatchers.any(Money.class));
        }

        @Test
        @DisplayName("AC-2: rejects transfer between different currencies")
        void shouldRejectDifferentCurrencies() {
            Money source = new Money(new BigDecimal("100.00"), "USD");
            Money destination = new Money(new BigDecimal("50.00"), "EUR");

            assertThatThrownBy(() -> transferService.transfer(source, destination, new BigDecimal("10.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Currencies must match");

            verifyNoInteractions(moneyAuditPort);
        }

        @Test
        @DisplayName("AC-3: rejects overdraft attempt")
        void shouldRejectOverdraft() {
            Money source = new Money(new BigDecimal("10.00"), "USD");
            Money destination = new Money(new BigDecimal("50.00"), "USD");

            assertThatThrownBy(() -> transferService.transfer(source, destination, new BigDecimal("10.01")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Insufficient funds");

            verifyNoInteractions(moneyAuditPort);
        }

        @Test
        @DisplayName("AC-4: zero transfer leaves balances unchanged")
        void shouldHandleZeroTransfer() {
            Money source = new Money(new BigDecimal("100.00"), "USD");
            Money destination = new Money(new BigDecimal("50.00"), "USD");

            TransferResult result = transferService.transfer(source, destination, BigDecimal.ZERO);

            assertThat(result.newSource()).isEqualTo(source);
            assertThat(result.newDestination()).isEqualTo(destination);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Edge-1: rejects null source")
        void shouldRejectNullSource() {
            Money destination = new Money(new BigDecimal("50.00"), "USD");

            assertThatThrownBy(() -> transferService.transfer(null, destination, BigDecimal.TEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Source must not be null");

            verifyNoInteractions(moneyAuditPort);
        }

        @Test
        @DisplayName("Edge-2: rejects null destination")
        void shouldRejectNullDestination() {
            Money source = new Money(new BigDecimal("100.00"), "USD");

            assertThatThrownBy(() -> transferService.transfer(source, null, BigDecimal.TEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Destination must not be null");

            verifyNoInteractions(moneyAuditPort);
        }

        @Test
        @DisplayName("Edge-3: rejects null amount")
        void shouldRejectNullAmount() {
            Money source = new Money(new BigDecimal("100.00"), "USD");
            Money destination = new Money(new BigDecimal("50.00"), "USD");

            assertThatThrownBy(() -> transferService.transfer(source, destination, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Transfer amount must not be null");

            verifyNoInteractions(moneyAuditPort);
        }

        @Test
        @DisplayName("Edge-4: rejects negative amount")
        void shouldRejectNegativeAmount() {
            Money source = new Money(new BigDecimal("100.00"), "USD");
            Money destination = new Money(new BigDecimal("50.00"), "USD");

            assertThatThrownBy(() -> transferService.transfer(source, destination, new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Transfer amount must not be negative");

            verifyNoInteractions(moneyAuditPort);
        }

        @Test
        @DisplayName("Edge-6: transfers exact balance leaving source at zero")
        void shouldTransferExactBalance() {
            Money source = new Money(new BigDecimal("100.00"), "USD");
            Money destination = new Money(new BigDecimal("50.00"), "USD");

            TransferResult result = transferService.transfer(source, destination, new BigDecimal("100.00"));

            assertThat(result.newSource()).isEqualTo(new Money(BigDecimal.ZERO, "USD"));
            assertThat(result.newDestination()).isEqualTo(new Money(new BigDecimal("150.00"), "USD"));
        }
    }
}
