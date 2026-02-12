import asyncio
from dataclasses import dataclass


@dataclass
class ProcessResult:
    exit_code: int
    stdout: str
    stderr: str
    timed_out: bool


async def run_process(command: list[str], cwd: str, timeout: int = 120) -> ProcessResult:
    """Run a subprocess asynchronously with timeout."""
    proc: asyncio.subprocess.Process | None = None
    try:
        proc = await asyncio.create_subprocess_exec(
            *command,
            cwd=cwd,
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
        )
        stdout, stderr = await asyncio.wait_for(proc.communicate(), timeout=timeout)
        return ProcessResult(
            exit_code=proc.returncode or 0,
            stdout=stdout.decode("utf-8", errors="replace"),
            stderr=stderr.decode("utf-8", errors="replace"),
            timed_out=False,
        )
    except asyncio.TimeoutError:
        if proc is not None:
            proc.kill()
        return ProcessResult(exit_code=-1, stdout="", stderr="Process timed out", timed_out=True)
    except Exception as e:
        return ProcessResult(exit_code=-1, stdout="", stderr=str(e), timed_out=False)


async def run_maven(project_dir: str, *goals: str, timeout: int = 300) -> ProcessResult:
    cmd = ["mvn", "-B", "--fail-at-end"] + list(goals)
    return await run_process(cmd, cwd=project_dir, timeout=timeout)


async def run_pytest(project_dir: str, *args: str, timeout: int = 120) -> ProcessResult:
    cmd = ["python", "-m", "pytest", "--tb=short", "-q"] + list(args)
    return await run_process(cmd, cwd=project_dir, timeout=timeout)


async def run_mutmut(project_dir: str, timeout: int = 600) -> ProcessResult:
    cmd = ["python", "-m", "mutmut", "run", "--no-progress"]
    return await run_process(cmd, cwd=project_dir, timeout=timeout)


async def run_mutmut_results(project_dir: str) -> ProcessResult:
    cmd = ["python", "-m", "mutmut", "results"]
    return await run_process(cmd, cwd=project_dir, timeout=30)


async def run_pit(project_dir: str, timeout: int = 600) -> ProcessResult:
    return await run_maven(project_dir, "test", "pitest:mutationCoverage", timeout=timeout)


async def run_playwright(work_dir: str, timeout: int = 120) -> ProcessResult:
    cmd = ["npx", "playwright", "test", "--reporter=json"]
    return await run_process(cmd, cwd=work_dir, timeout=timeout)
