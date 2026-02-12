package com.example.testdriver.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    @DisplayName("constructs money with normalized uppercase currency")
    void constructsMoneyWithNormalizedCurrency() {
        Money money = new Money(new BigDecimal("10.00"), " usd ");

        assertThat(money.currency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("rejects null amount")
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Money(null, "USD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be null");
    }

    @Test
    @DisplayName("rejects negative amount")
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-0.01"), "USD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be negative");
    }

    @Test
    @DisplayName("rejects blank currency")
    void rejectsBlankCurrency() {
        assertThatThrownBy(() -> new Money(new BigDecimal("10.00"), "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Currency must not be blank");
    }

    @Test
    @DisplayName("adds two amounts in same currency")
    void addsMoney() {
        Money base = new Money(new BigDecimal("10.00"), "USD");
        Money added = new Money(new BigDecimal("2.50"), "USD");

        Money result = base.add(added);

        assertThat(result).isEqualTo(new Money(new BigDecimal("12.50"), "USD"));
    }

    @Test
    @DisplayName("rejects add when currencies differ")
    void addRejectsMismatchedCurrency() {
        Money usd = new Money(new BigDecimal("10.00"), "USD");
        Money eur = new Money(new BigDecimal("1.00"), "EUR");

        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Currencies must match");
    }

    @Test
    @DisplayName("subtracts and allows zero result")
    void subtractsToZero() {
        Money left = new Money(new BigDecimal("5.00"), "USD");
        Money right = new Money(new BigDecimal("5.00"), "USD");

        Money result = left.subtract(right);

        assertThat(result).isEqualTo(new Money(BigDecimal.ZERO, "USD"));
    }

    @Test
    @DisplayName("rejects subtraction resulting in negative value")
    void rejectsNegativeSubtractResult() {
        Money left = new Money(new BigDecimal("3.00"), "USD");
        Money right = new Money(new BigDecimal("5.00"), "USD");

        assertThatThrownBy(() -> left.subtract(right))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Resulting amount must not be negative");
    }

    @Test
    @DisplayName("multiplies with decimal factor")
    void multipliesAmount() {
        Money base = new Money(new BigDecimal("12.00"), "USD");

        Money result = base.multiply(new BigDecimal("1.25"));

        assertThat(result).isEqualTo(new Money(new BigDecimal("15.0000"), "USD"));
    }

    @Test
    @DisplayName("rejects null multiplier")
    void rejectsNullMultiplier() {
        Money base = new Money(new BigDecimal("12.00"), "USD");

        assertThatThrownBy(() -> base.multiply(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Multiplier must not be null");
    }

    @Test
    @DisplayName("rejects negative multiplier")
    void rejectsNegativeMultiplier() {
        Money base = new Money(new BigDecimal("12.00"), "USD");

        assertThatThrownBy(() -> base.multiply(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Multiplier must not be negative");
    }

    @Test
    @DisplayName("multiplies with zero multiplier to get zero")
    void multiplyByZero() {
        Money base = new Money(new BigDecimal("99.99"), "USD");

        Money result = base.multiply(BigDecimal.ZERO);

        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("supports very large amounts")
    void supportsLargeValues() {
        Money huge = new Money(new BigDecimal("999999999999999999.99"), "USD");
        Money increment = new Money(new BigDecimal("0.01"), "USD");

        Money result = huge.add(increment);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("1000000000000000000.00"));
    }

    @Test
    @DisplayName("equals is reflexive")
    void equalsReflexive() {
        Money money = new Money(new BigDecimal("1.00"), "USD");

        assertThat(money).isEqualTo(money);
    }

    @Test
    @DisplayName("equals is symmetric")
    void equalsSymmetric() {
        Money first = new Money(new BigDecimal("1.0"), "USD");
        Money second = new Money(new BigDecimal("1.00"), "USD");

        assertThat(first.equals(second)).isTrue();
        assertThat(second.equals(first)).isTrue();
    }

    @Test
    @DisplayName("equals is transitive")
    void equalsTransitive() {
        Money a = new Money(new BigDecimal("3.00"), "USD");
        Money b = new Money(new BigDecimal("3.0"), "USD");
        Money c = new Money(new BigDecimal("3"), "USD");

        assertThat(a).isEqualTo(b);
        assertThat(b).isEqualTo(c);
        assertThat(a).isEqualTo(c);
    }

    @Test
    @DisplayName("equals returns false for null")
    void equalsAgainstNull() {
        Money money = new Money(new BigDecimal("1.00"), "USD");

        assertThat(money.equals(null)).isFalse();
    }

    @Test
    @DisplayName("equal objects share hash code")
    void equalObjectsShareHashCode() {
        Money first = new Money(new BigDecimal("7.0"), "USD");
        Money second = new Money(new BigDecimal("7.00"), "USD");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }
}
