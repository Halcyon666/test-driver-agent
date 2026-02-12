# Mutation Testing Rules

## What Is Mutation Testing?
Mutation testing injects bugs (mutants) into your code and checks if tests catch them.
- Killed mutant = your test caught the bug (good)
- Survived mutant = your test missed the bug (bad)
- Mutation score = killed / total mutants

## When to Run
- After completing a feature's test suite
- Before marking a spec as "done"
- In CI on every PR (automated via GitHub Actions)

## How to Run
```bash
# Full mutation testing
mvn pitest:mutationCoverage

# View report
# Open target/pit-reports/index.html
```

## Thresholds (CI Gates)
- mutationThreshold: 60% (build fails below this)
- coverageThreshold: 80% (line coverage minimum)
- Target for critical logic: 80%+ mutation score

## Interpreting Results

### Survived Mutants — What to Do
| Mutant Type | Example | Fix |
|-------------|---------|-----|
| Changed conditional boundary | `<` → `<=` | Add boundary test (test exact boundary value) |
| Negated conditional | `if (x > 0)` → `if (x <= 0)` | Add test for both sides of condition |
| Replaced return value | `return result` → `return null` | Assert return value is not null AND correct |
| Removed method call | `audit.record(...)` removed | Use `verify()` to check the call happened |
| Changed math operator | `+` → `-` | Assert the exact numeric result |

### Acceptable Survivors
- Mutants in toString() methods
- Mutants in logging statements
- Mutants in Spring configuration classes
- Use `excludedClasses` or `excludedMethods` in pom.xml for these

## Anti-Patterns
- Do NOT lower thresholds to make CI pass
- Do NOT exclude classes just because they have surviving mutants
- Do NOT write assertion-free tests to boost coverage
- Do NOT confuse line coverage with mutation score (95% coverage can have 30% mutation score)
