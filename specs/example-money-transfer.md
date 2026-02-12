# Feature Spec: Money Transfer

## 1. Summary
Transfer money between two Money objects, deducting from source and adding to destination, with currency matching enforcement.

## 2. Acceptance Criteria

- [x] AC-1: Given two Money objects with the same currency, when transfer is called, then source is reduced and destination is increased by the transfer amount
- [x] AC-2: Given two Money objects with different currencies, when transfer is called, then IllegalArgumentException is thrown
- [x] AC-3: Given a transfer amount greater than source balance, when transfer is called, then IllegalArgumentException is thrown
- [x] AC-4: Given a zero transfer amount, when transfer is called, then both balances remain unchanged

## 3. Input/Output Contract

```java
// Method signature
TransferResult transfer(Money source, Money destination, BigDecimal amount);

// Valid inputs
// - source: non-null Money with amount >= transferAmount
// - destination: non-null Money with same currency as source
// - amount: non-null, >= 0

// Expected outputs
// - returns: TransferResult with newSource and newDestination

// Error conditions
// - throws IllegalArgumentException when source is null
// - throws IllegalArgumentException when destination is null
// - throws IllegalArgumentException when amount is null or negative
// - throws IllegalArgumentException when currencies don't match
// - throws IllegalArgumentException when source.amount < amount
```

## 4. Edge Cases & Boundaries

| # | Condition | Expected Behavior |
|---|-----------|-------------------|
| 1 | Null source | IllegalArgumentException("Source must not be null") |
| 2 | Null destination | IllegalArgumentException("Destination must not be null") |
| 3 | Null amount | IllegalArgumentException("Transfer amount must not be null") |
| 4 | Negative amount | IllegalArgumentException("Transfer amount must not be negative") |
| 5 | Zero amount | Both balances unchanged |
| 6 | Exact balance transfer | Source becomes zero, destination increases |
| 7 | Currency mismatch | IllegalArgumentException("Currencies must match") |
| 8 | Overdraft attempt | IllegalArgumentException("Insufficient funds") |

## 5. Test Plan

| Spec Item | Test Class | Test Method |
|-----------|-----------|-------------|
| AC-1 | MoneyTransferServiceTest | shouldTransferBetweenSameCurrency() |
| AC-2 | MoneyTransferServiceTest | shouldRejectDifferentCurrencies() |
| AC-3 | MoneyTransferServiceTest | shouldRejectOverdraft() |
| AC-4 | MoneyTransferServiceTest | shouldHandleZeroTransfer() |
| Edge-1 | MoneyTransferServiceTest | shouldRejectNullSource() |
| Edge-2 | MoneyTransferServiceTest | shouldRejectNullDestination() |
| Edge-3 | MoneyTransferServiceTest | shouldRejectNullAmount() |
| Edge-4 | MoneyTransferServiceTest | shouldRejectNegativeAmount() |
| Edge-6 | MoneyTransferServiceTest | shouldTransferExactBalance() |

## 6. Mutation Testing Targets

- [x] Arithmetic: subtract from source, add to destination (swap +/- would be caught)
- [x] Boundary: compareTo checks (< vs <=)
- [x] Null guards: all 3 parameters
- [x] Currency comparison: equals check

## 7. Done Criteria
- [x] All acceptance criteria have passing tests
- [x] All edge cases have passing tests
- [x] `mvn test` passes
- [x] `mvn pitest:mutationCoverage` passes with mutation score >= 60%
- [x] No surviving mutants in transfer logic
