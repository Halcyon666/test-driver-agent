package com.example.testdriver.service;

import com.example.testdriver.domain.Money;

public interface MoneyAuditPort {

    void recordOperation(String operation, Money source, Money result);
}
