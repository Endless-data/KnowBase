import { apiClient, request } from './client';
import type {
  BatchDocumentDeleteResponse,
  BatchDocumentUploadResponse,
  DocumentItem,
  DocumentUploadResult,
} from '../types/api';

export function listDocuments(): Promise<DocumentItem[]> {
  return request(apiClient.get('/api/documents'));
}

export function uploadDocument(file: File): Promise<DocumentUploadResult> {
  const formData = new FormData();
  formData.append('file', file);
  return request(apiClient.post('/api/documents/upload', formData));
}

export function uploadDocuments(files: File[]): Promise<BatchDocumentUploadResponse> {
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  return request(apiClient.post('/api/documents/batch-upload', formData));
}

export function deleteDocument(id: number): Promise<void> {
  return request(apiClient.delete(`/api/documents/${id}`));
}

export function deleteDocuments(documentIds: number[]): Promise<BatchDocumentDeleteResponse> {
  return request(apiClient.post('/api/documents/batch-delete', { documentIds }));
}
