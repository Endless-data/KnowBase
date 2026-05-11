import { apiClient, request } from './client';
import type { HistoryDetail, HistoryListItem } from '../types/api';

export function listHistory(): Promise<HistoryListItem[]> {
  return request(apiClient.get('/api/history'));
}

export function getHistory(id: number): Promise<HistoryDetail> {
  return request(apiClient.get(`/api/history/${id}`));
}

export function deleteHistory(id: number): Promise<void> {
  return request(apiClient.delete(`/api/history/${id}`));
}
