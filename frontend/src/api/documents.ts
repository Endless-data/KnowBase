import { apiClient, request } from './client';
import type { DocumentItem, DocumentUploadResult } from '../types/api';

export function listDocuments(): Promise<DocumentItem[]> {
  return request(apiClient.get('/api/documents'));
}

export function uploadDocument(file: File): Promise<DocumentUploadResult> {
  const formData = new FormData();
  formData.append('file', file);
  return request(
    apiClient.post('/api/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }),
  );
}

export function deleteDocument(id: number): Promise<void> {
  return request(apiClient.delete(`/api/documents/${id}`));
}
