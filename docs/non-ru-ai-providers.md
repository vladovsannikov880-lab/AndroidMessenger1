# Нероссийские нейросети для твоего приложения и как интегрировать

## Что уже сделано в коде

`AiClient` теперь поддерживает OpenAI-совместимые API через `local.properties`:

- `AI_PROVIDER` = `openai` | `mistral` | `openrouter`
- `AI_API_KEY` = API ключ выбранного провайдера
- `AI_MODEL` = (опционально) ID модели
- `AI_BASE_URL` = (опционально) свой endpoint

## Рекомендуемые провайдеры для генерации текста (Level 1)

1. **OpenAI**
   - Endpoint: `https://api.openai.com/v1/chat/completions`
   - Хороший баланс качества/скорости.

2. **Mistral AI**
   - Endpoint: `https://api.mistral.ai/v1/chat/completions`
   - Удобен как европейская альтернатива.

3. **OpenRouter**
   - Endpoint: `https://openrouter.ai/api/v1/chat/completions`
   - Единый шлюз ко многим моделям.

## Для генерации изображений (Level 2)

1. **OpenAI Images API** (gpt-image-1)
2. **Google Gemini API** (image-capable модели)
3. **Stability AI API** (Stable Diffusion)

> Для image generation лучше добавить отдельный `ImageClient` и хранить URL/bytes в Firebase Storage.

## Пример `local.properties`

```properties
AI_PROVIDER=openai
AI_API_KEY=sk-...
AI_MODEL=gpt-4o-mini
# AI_BASE_URL=   # оставь пустым для дефолта
```

### Пример для Mistral

```properties
AI_PROVIDER=mistral
AI_API_KEY=...
AI_MODEL=mistral-small-latest
```

### Пример для OpenRouter

```properties
AI_PROVIDER=openrouter
AI_API_KEY=...
AI_MODEL=openai/gpt-4o-mini
```

## Важный production-совет

Не держи реальные API-ключи на клиенте Android в релизе.
Сделай backend/proxy (Cloud Functions/свой сервер), а приложение пусть обращается к твоему безопасному API.
