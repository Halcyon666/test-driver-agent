# Mutation Testing Rules

## What Is Mutation Testing?
Mutation testing injects bugs into your code and checks if tests catch them.
- Killed mutant = test caught the bug (good)
- Survived mutant = test missed the bug (bad)
- Mutation score = killed / total mutants

## Tools by Language
- Java: PIT (pitest) via `mvn pitest:mutationCoverage`
- Python: mutmut via `mutmut run` then `mutmut results`
- JS/TS: StrykerJS via `npx stryker run`

## Thresholds
- Minimum: 60% mutation score
- Target for critical logic: 80%+
- 95% line coverage with 30% mutation score = your tests are lying

## Anti-Patterns
- Do NOT lower thresholds to make CI pass
- Do NOT exclude classes just because they have surviving mutants
- Do NOT write assertion-free tests to boost coverage
