import { API_BASE_URL, apiClient, request } from './client';
import type { AskResponse, QaStreamEvent } from '../types/api';

export function askQuestion(question: string): Promise<AskResponse> {
  return request(apiClient.post('/api/qa/ask', { question }));
}

interface AskQuestionStreamHandlers {
  onEvent: (event: QaStreamEvent) => void;
  onError: (message: string) => void;
}

export async function askQuestionStream(question: string, handlers: AskQuestionStreamHandlers): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/qa/ask/stream`, {
    body: JSON.stringify({ question }),
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'POST',
  });

  if (!response.ok || !response.body) {
    handlers.onError(await resolveErrorMessage(response));
    return;
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }

    buffer += decoder.decode(value, { stream: true });
    const events = buffer.split('\n\n');
    buffer = events.pop() ?? '';
    events.forEach((eventText) => handleSseEvent(eventText, handlers));
  }

  if (buffer.trim()) {
    handleSseEvent(buffer, handlers);
  }
}

function handleSseEvent(eventText: string, handlers: AskQuestionStreamHandlers) {
  const data = eventText
    .split('\n')
    .filter((line) => line.startsWith('data:'))
    .map((line) => line.slice('data:'.length).trim())
    .join('');
  if (!data) {
    return;
  }

  try {
    handlers.onEvent(JSON.parse(data) as QaStreamEvent);
  } catch {
    handlers.onError('解析流式回答失败');
  }
}

async function resolveErrorMessage(response: Response) {
  try {
    const body = (await response.json()) as { message?: string };
    return body.message || '提问失败，请稍后重试';
  } catch {
    return response.statusText || '提问失败，请稍后重试';
  }
}
