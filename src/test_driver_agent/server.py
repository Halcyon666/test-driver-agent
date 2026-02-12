from mcp.server.fastmcp import FastMCP

from . import prompts
from .config import Config
from .executor import (
    ProcessResult,
    run_maven,
    run_mutmut,
    run_mutmut_results,
    run_pit,
    run_playwright,
    run_process,
    run_pytest,
)
from .llm_client import LlmClient

mcp = FastMCP("test-driver-agent")

_config: Config | None = None
_llm: LlmClient | None = None


def _get_config() -> Config:
    global _config
    if _config is None:
        _config = Config.from_env()
    return _config


def _get_llm() -> LlmClient:
    global _llm
    if _llm is None:
        _llm = LlmClient(_get_config())
    return _llm


@mcp.tool()
def generate_tdd_tests(spec: str, source_code: str, language: str = "java") -> str:
    """Generate TDD test suite from specification and source code.

    Produces comprehensive test code following red-green-refactor pattern
    with strong assertions, edge cases, and boundary tests.
    Supports Java (JUnit 5 + AssertJ) and Python (pytest).
    """
    llm = _get_llm()
    user_prompt = (
        f"## Specification\n{spec}\n\n"
        f"## Source Code ({language})\n```{language}\n{source_code}\n```"
    )
    return llm.generate(prompts.TDD_TEST_GENERATION, user_prompt)


@mcp.tool()
def generate_api_tests(endpoint_spec: str, controller_code: str, language: str = "java") -> str:
    """Generate REST API integration tests from endpoint specification.

    Produces MockMvc/RestAssured (Java) or httpx/pytest (Python) tests
    covering happy path, validation errors, auth, and edge cases.
    """
    llm = _get_llm()
    user_prompt = (
        f"## Endpoint Specification\n{endpoint_spec}\n\n"
        f"## Controller Code ({language})\n```{language}\n{controller_code}\n```"
    )
    return llm.generate(prompts.API_TEST_GENERATION, user_prompt)


@mcp.tool()
def generate_playwright_tests(ui_spec: str, base_url: str = "http://localhost:3000") -> str:
    """Generate Playwright TypeScript browser automation tests.

    Produces executable .spec.ts files with page object patterns,
    proper waits, data-testid selectors, and screenshot-on-failure.
    """
    llm = _get_llm()
    user_prompt = f"## UI Test Specification\n{ui_spec}\n\n## Base URL\n{base_url}"
    return llm.generate(prompts.PLAYWRIGHT_TEST_GENERATION, user_prompt)


@mcp.tool()
async def run_mutation_analysis(project_dir: str, language: str = "java") -> str:
    """Run mutation testing on a project and return analysis.

    For Java: runs PIT (pitest) via Maven.
    For Python: runs mutmut.
    Reports mutation score, survived mutants, and suggests test improvements.
    """
    if language == "java":
        result = await run_pit(project_dir)
    else:
        result = await run_mutmut(project_dir)
        if result.exit_code != -1:
            details = await run_mutmut_results(project_dir)
            result = ProcessResult(
                exit_code=result.exit_code,
                stdout=result.stdout + "\n\n--- RESULTS ---\n" + details.stdout,
                stderr=result.stderr,
                timed_out=False,
            )

    report = "## Mutation Testing Report\n\n"
    report += f"**Exit code**: {result.exit_code}\n"
    report += f"**Timed out**: {result.timed_out}\n\n"

    if result.timed_out:
        report += "Mutation testing timed out. Try reducing scope with targetClasses/targetTests.\n"
        return report

    report += f"### Output\n```\n{result.stdout[-3000:]}\n```\n"
    if result.stderr:
        report += f"\n### Errors\n```\n{result.stderr[-1000:]}\n```\n"

    if "survived" in result.stdout.lower() or "SURVIVED" in result.stdout:
        llm = _get_llm()
        suggestions = llm.generate(
            prompts.MUTATION_IMPROVEMENT,
            f"## Mutation Testing Output\n```\n{result.stdout[-4000:]}\n```",
        )
        report += f"\n### Improvement Suggestions\n{suggestions}\n"

    return report


@mcp.tool()
def analyze_test_quality(test_code: str, production_code: str) -> str:
    """Analyze test suite quality: assertion density, test smells, coverage gaps.

    Provides actionable recommendations for improving test effectiveness.
    Checks for: assertion roulette, eager tests, mystery guests, missing edge cases.
    """
    llm = _get_llm()
    user_prompt = (
        f"## Test Code\n```\n{test_code}\n```\n\n"
        f"## Production Code\n```\n{production_code}\n```"
    )
    return llm.generate(prompts.TEST_QUALITY_ANALYSIS, user_prompt)


@mcp.tool()
async def run_full_test_cycle(
    spec: str,
    source_code: str,
    project_dir: str,
    language: str = "java",
) -> str:
    """Run the full test cycle: generate → compile → test → mutation test → improve.

    Iterates up to 3 times to improve mutation score above 70%.
    This is the main orchestration tool that chains all other capabilities.
    """
    config = _get_config()
    llm = _get_llm()
    max_iterations = config.max_retries
    report_parts: list[str] = []

    test_code = generate_tdd_tests(spec, source_code, language)
    report_parts.append(
        f"## Iteration 1: Initial Generation\n\n```{language}\n{test_code}\n```\n"
    )

    for iteration in range(1, max_iterations + 1):
        if language == "java":
            compile_result = await run_maven(project_dir, "test-compile")
        else:
            compile_result = await run_pytest(project_dir, "--collect-only")

        if compile_result.exit_code != 0:
            report_parts.append(
                f"### Compile/collect failed (iteration {iteration})\n```\n"
                f"{compile_result.stderr[-2000:]}\n```\n"
            )
            fix_prompt = (
                "The following test code failed to compile:\n"
                f"```\n{test_code}\n```\n\n"
                f"Errors:\n```\n{compile_result.stderr[-2000:]}\n```\n\n"
                "Fix the test code. Output ONLY the corrected test code."
            )
            test_code = llm.generate(prompts.TDD_TEST_GENERATION, fix_prompt)
            continue

        if language == "java":
            test_result = await run_maven(project_dir, "test")
        else:
            test_result = await run_pytest(project_dir)

        report_parts.append(f"### Test run (iteration {iteration}): exit={test_result.exit_code}\n")

        if language == "java":
            mut_result = await run_pit(project_dir)
        else:
            mut_result = await run_mutmut(project_dir)

        report_parts.append(
            f"### Mutation testing (iteration {iteration})\n```\n{mut_result.stdout[-2000:]}\n```\n"
        )

        if "100%" in mut_result.stdout or iteration == max_iterations:
            break

        improve_prompt = (
            f"## Current test code\n```\n{test_code}\n```\n\n"
            f"## Surviving mutants\n```\n{mut_result.stdout[-3000:]}\n```\n\n"
            "Generate improved test code that kills the surviving mutants. "
            "Output ONLY the complete test code."
        )
        test_code = llm.generate(prompts.MUTATION_IMPROVEMENT, improve_prompt)
        report_parts.append(
            f"## Iteration {iteration + 1}: Improved tests\n\n```{language}\n{test_code}\n```\n"
        )

    return "\n".join(report_parts)
