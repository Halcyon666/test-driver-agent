import os
from dataclasses import dataclass


@dataclass
class Config:
    openai_api_key: str
    openai_base_url: str
    model: str
    temperature: float
    max_retries: int
    process_timeout: int

    @classmethod
    def from_env(cls) -> "Config":
        return cls(
            openai_api_key=os.environ.get("OPENAI_API_KEY", ""),
            openai_base_url=os.environ.get("LLM_BASE_URL", "https://api.openai.com/v1"),
            model=os.environ.get("LLM_MODEL", "gpt-4o"),
            temperature=float(os.environ.get("LLM_TEMPERATURE", "0.2")),
            max_retries=int(os.environ.get("MAX_RETRIES", "3")),
            process_timeout=int(os.environ.get("PROCESS_TIMEOUT", "120")),
        )
