from openai import OpenAI

from .config import Config


class LlmClient:
    def __init__(self, config: Config):
        self._client = OpenAI(
            api_key=config.openai_api_key,
            base_url=config.openai_base_url,
        )
        self._model = config.model
        self._temperature = config.temperature

    def generate(self, system_prompt: str, user_prompt: str) -> str:
        response = self._client.chat.completions.create(
            model=self._model,
            temperature=self._temperature,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
        )
        return response.choices[0].message.content or ""
