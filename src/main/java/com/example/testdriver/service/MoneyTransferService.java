package com.example.testdriver.service;

import com.example.testdriver.domain.Money;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MoneyTransferService {

    private final MoneyAuditPort moneyAuditPort;

    public MoneyTransferService(MoneyAuditPort moneyAuditPort) {
        this.moneyAuditPort = moneyAuditPort;
    }

    public TransferResult transfer(Money source, Money destination, BigDecimal amount) {
        if (source == null) {
            throw new IllegalArgumentException("Source must not be null");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination must not be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Transfer amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transfer amount must not be negative");
        }
        if (!source.currency().equals(destination.currency())) {
            throw new IllegalArgumentException("Currencies must match");
        }
        if (source.amount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        Money transferMoney = new Money(amount, source.currency());
        Money newSource = source.subtract(transferMoney);
        Money newDestination = destination.add(transferMoney);

        moneyAuditPort.recordOperation("TRANSFER", source, newSource);
        return new TransferResult(newSource, newDestination);
    }
}
