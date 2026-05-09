export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface DocumentItem {
  id: number;
  name: string;
  fileType: string;
  status: string;
  createdAt: string;
}

export interface DocumentUploadResult {
  documentId: number;
  name: string;
  status: string;
}

export interface AskRequest {
  question: string;
}

export interface Citation {
  chunkId: number;
  documentId: number;
  documentName: string;
  chunkIndex: number;
  content: string;
}

export interface AskResponse {
  answer: string;
  citations: Citation[];
}

export interface HistoryListItem {
  id: number;
  question: string;
  answer: string;
  retrievalCount: number;
  createdAt: string;
}

export interface HistoryDetail extends HistoryListItem {
  citations: Citation[];
}
