# Feature Spec: [Feature Name]

## 1. Summary
<!-- One sentence: what does this feature do and why? -->

## 2. Acceptance Criteria
<!-- Each criterion becomes one or more test cases. Be specific. -->

- [ ] AC-1: [Given X, when Y, then Z]
- [ ] AC-2: [Given X, when Y, then Z]
- [ ] AC-3: [Edge case: ...]

## 3. Input/Output Contract
<!-- Define the exact interface. This drives the test signatures. -->

```java
// Method signature
ReturnType methodName(ParamType param);

// Valid inputs
// - param: non-null, range [0, 100]

// Expected outputs
// - returns: non-null, satisfies [invariant]

// Error conditions
// - throws IllegalArgumentException when param is null
// - throws IllegalArgumentException when param < 0
```

## 4. Edge Cases & Boundaries
<!-- List every boundary. Each one becomes a test. -->

| # | Condition | Expected Behavior |
|---|-----------|-------------------|
| 1 | Null input | IllegalArgumentException |
| 2 | Zero value | [specific behavior] |
| 3 | Max value | [specific behavior] |
| 4 | Negative value | [specific behavior] |

## 5. Test Plan (generated from above)
<!-- Map each AC and edge case to a test method name -->

| Spec Item | Test Class | Test Method |
|-----------|-----------|-------------|
| AC-1 | FeatureTest | shouldDoXWhenY() |
| AC-2 | FeatureTest | shouldDoAWhenB() |
| Edge-1 | FeatureTest | shouldRejectNullInput() |

## 6. Mutation Testing Targets
<!-- Which logic is most critical to verify with mutation testing? -->

- [ ] Arithmetic operations (comparisons, operators)
- [ ] Boundary checks (< vs <=, > vs >=)
- [ ] Null/empty guards
- [ ] Return value correctness

## 7. Done Criteria
- [ ] All acceptance criteria have passing tests
- [ ] All edge cases have passing tests
- [ ] `mvn test` passes
- [ ] `mvn pitest:mutationCoverage` passes with mutation score >= 60%
- [ ] No surviving mutants in critical logic
