# Test Driver Agent

An MCP (Model Context Protocol) server for full test automation — TDD test generation, RPA browser testing, API integration tests, mutation analysis, and test quality verification.

## Features

- **TDD Test Generation** — Generate JUnit 5 (Java), pytest (Python), or vitest (TypeScript) test suites from specifications and source code
- **API Integration Tests** — Generate REST API tests for MockMvc, RestAssured, or httpx
- **Playwright RPA** — Generate TypeScript browser automation tests with page object patterns
- **Mutation Testing** — Run PIT (Java) or mutmut (Python), analyze surviving mutants
- **Test Quality Analysis** — Detect assertion density, test smells, coverage gaps
- **Full Test Cycle** — Orchestrate generate → compile → test → mutate → improve loop

## Installation

```bash
pip install -e ".[dev]"
```

Requires Python 3.10+ and an OpenAI-compatible API key.

## Configuration

Set environment variables or create a `.env` file:

```bash
OPENAI_API_KEY=sk-your-api-key
LLM_BASE_URL=https://api.openai.com/v1      # Optional: use local/custom LLM
LLM_MODEL=gpt-4o                            # Optional: default gpt-4o
LLM_TEMPERATURE=0.2                          # Optional: default 0.2
MAX_RETRIES=3                                 # Optional: max iterations for full cycle
PROCESS_TIMEOUT=120                            # Optional: subprocess timeout in seconds
```

## Usage

### Run as MCP Server

```bash
# Basic (stdio transport - for Claude Desktop, Cursor, etc.)
python -m test_driver_agent

# Or use the entry point
test-driver-agent
```

### Configure in Claude Desktop

Add to `~/.config/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "test-driver-agent": {
      "command": "python",
      "args": ["-m", "test_driver_agent"],
      "env": {
        "OPENAI_API_KEY": "sk-...",
        "LLM_MODEL": "gpt-4o"
      }
    }
  }
}
```

### Configure in Cursor

Add to `.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "test-driver-agent": {
      "command": "python",
      "args": ["-m", "test_driver_agent"],
      "env": {
        "OPENAI_API_KEY": "sk-..."
      }
    }
  }
}
```

## MCP Tools

### generate_tdd_tests

Generate unit tests from specification and source code.

```python
# Example call from MCP client
result = await mcp.call_tool("generate_tdd_tests", {
    "spec": "Feature: User registration\nAs a new user\nI want to register with email and password\nSo that I can access the system",
    "source_code": "class UserRegistration:\n    def register(self, email: str, password: str) -> User:\n        ...",
    "language": "java"  # or "python", "typescript"
})
```

### generate_api_tests

Generate REST API integration tests from endpoint specifications.

```python
result = await mcp.call_tool("generate_api_tests", {
    "endpoint_spec": "POST /api/users\n\nRequest: {email, password}\nResponse: 201 Created with user object",
    "controller_code": "@PostMapping(\"/users\")\npublic User createUser(@RequestBody UserRequest req) { ... }",
    "language": "java"
})
```

### generate_playwright_tests

Generate Playwright browser automation tests.

```python
result = await mcp.call_tool("generate_playwright_tests", {
    "ui_spec": "User registration flow:\n1. Navigate to /register\n2. Fill email, password fields\n3. Click submit\n4. Verify redirect to dashboard",
    "base_url": "http://localhost:3000"
})
```

### run_mutation_analysis

Run mutation testing on a project and get improvement suggestions.

```python
result = await mcp.call_tool("run_mutation_analysis", {
    "project_dir": "/path/to/java/project",
    "language": "java"  # or "python"
})
```

### analyze_test_quality

Analyze test suite quality and get recommendations.

```python
result = await mcp.call_tool("analyze_test_quality", {
    "test_code": "@Test\nvoid shouldRegisterUser() { ... }",
    "production_code": "public User register(String email) { ... }"
})
```

### run_full_test_cycle

Orchestrate the complete test pipeline with iteration.

```python
result = await mcp.call_tool("run_full_test_cycle", {
    "spec": "Feature: Money transfer...",
    "source_code": "class MoneyTransfer { ... }",
    "project_dir": "/path/to/project",
    "language": "java"
})
```

## Project Structure

```
test-driver-agent/
├── src/test_driver_agent/
│   ├── __init__.py          # Version
│   ├── __main__.py          # Entry point
│   ├── config.py            # Environment config
│   ├── llm_client.py        # OpenAI-compatible LLM wrapper
│   ├── executor.py          # Subprocess runner (mvn, pytest, npx)
│   ├── prompts.py           # Detailed LLM prompt templates
│   └── server.py            # FastMCP server with 6 tools
├── specs/                   # Feature spec templates
├── .cursor/rules/           # TDD workflow rules for Cursor
├── pyproject.toml           # Package config
└── .env.example             # Environment template
```

## Requirements

- Python 3.10+
- OpenAI-compatible API (OpenAI, Anthropic via proxy, Ollama, etc.)
- For Java projects: Maven 3.6+, PIT plugin
- For Python projects: pytest, mutmut
- For Playwright tests: Node.js, npx playwright

## License

MIT
