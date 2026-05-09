import { apiClient, request } from './client';
import type { AskResponse } from '../types/api';

export function askQuestion(question: string): Promise<AskResponse> {
  return request(apiClient.post('/api/qa/ask', { question }));
}
