package com.example.testdriver.service;

import com.example.testdriver.domain.Money;
import org.springframework.stereotype.Component;

@Component
public class NoOpMoneyAuditAdapter implements MoneyAuditPort {

    @Override
    public void recordOperation(String operation, Money source, Money result) {
        // Intentionally no-op for sample project wiring.
    }
}
