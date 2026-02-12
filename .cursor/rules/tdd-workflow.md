# TDD Workflow Rules

## Core Principle
Tests are the source of truth. Code exists to make tests pass. Never write implementation before tests.

## Workflow (MANDATORY for every feature)

### Step 1: Spec First
- Before writing ANY code, create or update a spec in `specs/` using `specs/TEMPLATE.md`
- Define acceptance criteria, input/output contracts, and edge cases
- Each acceptance criterion maps to at least one test

### Step 2: Write Failing Tests
- Create test class BEFORE implementation class
- Write ALL test methods from the spec's test plan
- Each test must:
  - Have a `@DisplayName` describing the behavior (not the method)
  - Use AssertJ assertions (`assertThat(...)`)
  - Test exactly ONE behavior
  - Follow naming: `shouldDoXWhenY()` or `rejectsXWhenY()`
- Run tests — they MUST fail (red phase)

### Step 3: Implement Minimally
- Write the MINIMUM code to make tests pass
- Do NOT add logic that isn't tested
- Do NOT optimize prematurely
- Run tests — they MUST pass (green phase)

### Step 4: Refactor
- Clean up implementation while keeping tests green
- Extract methods, rename for clarity, remove duplication
- Run tests after EVERY refactor step

### Step 5: Mutation Testing
- Run `mvn pitest:mutationCoverage`
- Check the HTML report in `target/pit-reports/`
- If mutation score < 60%: add more assertions or edge case tests
- Target: kill ALL mutants in critical business logic

## Test Quality Rules

### Assertions
- NEVER write a test without assertions
- NEVER use `assertTrue(true)` or equivalent no-ops
- ALWAYS assert the specific value, not just "no exception thrown"
- Use `assertThat(result).isEqualTo(expected)` over `assertEquals`
- For exceptions: `assertThatThrownBy(...).isInstanceOf(...).hasMessage(...)`

### Structure
- Unit tests: `*Test.java` in same package as production code
- Integration tests: `*IT.java` with `@SpringBootTest`
- One test class per production class
- Group related tests with `@Nested` if > 10 tests

### Mocking
- Use `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`
- Verify interactions with `verify()` for side effects
- Use `verifyNoInteractions()` on error paths
- NEVER mock the class under test

### What NOT to Do
- Do NOT delete or skip failing tests
- Do NOT write tests after implementation
- Do NOT suppress warnings or errors
- Do NOT use `@Disabled` without a linked issue
- Do NOT write tests that pass regardless of implementation (false greens)
