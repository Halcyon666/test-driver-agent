package com.example.testdriver.service;

import com.example.testdriver.domain.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MoneyService {

    private final MoneyAuditPort moneyAuditPort;

    public MoneyService(MoneyAuditPort moneyAuditPort) {
        this.moneyAuditPort = moneyAuditPort;
    }

    public Money applyGrowth(Money base, BigDecimal ratePercent) {
        if (base == null) {
            throw new IllegalArgumentException("Base money must not be null");
        }
        if (ratePercent == null) {
            throw new IllegalArgumentException("Rate percent must not be null");
        }
        if (ratePercent.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rate percent must not be negative");
        }

        BigDecimal multiplier = BigDecimal.ONE.add(ratePercent.movePointLeft(2));
        Money result = base.multiply(multiplier);
        moneyAuditPort.recordOperation("GROWTH", base, result);
        return result;
    }

    public Money applyDiscount(Money base, BigDecimal discountPercent) {
        if (base == null) {
            throw new IllegalArgumentException("Base money must not be null");
        }
        if (discountPercent == null) {
            throw new IllegalArgumentException("Discount percent must not be null");
        }
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }

        BigDecimal discountRatio = discountPercent.movePointLeft(2);
        BigDecimal retained = BigDecimal.ONE.subtract(discountRatio);
        Money result = base.multiply(retained);
        moneyAuditPort.recordOperation("DISCOUNT", base, result);
        return result;
    }
}
