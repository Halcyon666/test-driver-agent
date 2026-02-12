package com.example.testdriver.service;

import com.example.testdriver.domain.Money;

import java.math.BigDecimal;

public record TransferResult(Money newSource, Money newDestination) {
}
