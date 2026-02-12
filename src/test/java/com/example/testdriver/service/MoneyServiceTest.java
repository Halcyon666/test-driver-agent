package com.example.testdriver.service;

import com.example.testdriver.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MoneyServiceTest {

    @Mock
    private MoneyAuditPort moneyAuditPort;

    @InjectMocks
    private MoneyService moneyService;

    @Test
    @DisplayName("applyGrowth increases amount and records audit")
    void applyGrowthIncreasesAmountAndAudits() {
        Money base = new Money(new BigDecimal("100.00"), "USD");

        Money result = moneyService.applyGrowth(base, new BigDecimal("10"));

        assertThat(result).isEqualTo(new Money(new BigDecimal("110.00"), "USD"));

        ArgumentCaptor<Money> resultCaptor = ArgumentCaptor.forClass(Money.class);
        verify(moneyAuditPort).recordOperation(org.mockito.ArgumentMatchers.eq("GROWTH"), org.mockito.ArgumentMatchers.eq(base), resultCaptor.capture());
        assertThat(resultCaptor.getValue()).isEqualTo(result);
    }

    @Test
    @DisplayName("applyGrowth rejects negative percent and does not audit")
    void applyGrowthRejectsNegativeRate() {
        Money base = new Money(new BigDecimal("100.00"), "USD");

        assertThatThrownBy(() -> moneyService.applyGrowth(base, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rate percent must not be negative");

        verifyNoInteractions(moneyAuditPort);
    }

    @Test
    @DisplayName("applyDiscount decreases amount and records audit")
    void applyDiscountDecreasesAmountAndAudits() {
        Money base = new Money(new BigDecimal("200.00"), "USD");

        Money result = moneyService.applyDiscount(base, new BigDecimal("25"));

        assertThat(result).isEqualTo(new Money(new BigDecimal("150.00"), "USD"));
        verify(moneyAuditPort).recordOperation("DISCOUNT", base, result);
    }

    @Test
    @DisplayName("applyDiscount rejects values above 100")
    void applyDiscountRejectsValuesAboveHundred() {
        Money base = new Money(new BigDecimal("200.00"), "USD");

        assertThatThrownBy(() -> moneyService.applyDiscount(base, new BigDecimal("100.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Discount percent must be between 0 and 100");

        verifyNoInteractions(moneyAuditPort);
    }

    @Test
    @DisplayName("applyDiscount with hundred percent returns zero")
    void applyDiscountHundredPercent() {
        Money base = new Money(new BigDecimal("200.00"), "USD");

        Money result = moneyService.applyDiscount(base, new BigDecimal("100"));

        assertThat(result).isEqualTo(new Money(BigDecimal.ZERO, "USD"));
        verify(moneyAuditPort).recordOperation("DISCOUNT", base, result);
    }
}
