# TDD Workflow Rules

## Core Principle
Tests are the source of truth. Code exists to make tests pass. Never write implementation before tests.

## Workflow (MANDATORY for every feature)

### Step 1: Spec First
- Before writing ANY code, create or update a spec in `specs/` using `specs/TEMPLATE.md`
- Define acceptance criteria, input/output contracts, and edge cases
- Each acceptance criterion maps to at least one test

### Step 2: Write Failing Tests
- Create test file BEFORE implementation
- Write ALL test functions from the spec's test plan
- Each test must have a descriptive name and strong assertions
- Run tests — they MUST fail (red phase)

### Step 3: Implement Minimally
- Write the MINIMUM code to make tests pass
- Run tests — they MUST pass (green phase)

### Step 4: Refactor
- Clean up while keeping tests green
- Run tests after EVERY refactor step

### Step 5: Mutation Testing
- Java: `mvn pitest:mutationCoverage`
- Python: `mutmut run` then `mutmut results`
- Target: 60%+ mutation score, kill ALL mutants in critical logic

## What NOT to Do
- Do NOT delete or skip failing tests
- Do NOT write tests after implementation
- Do NOT write assertion-free tests or false greens
