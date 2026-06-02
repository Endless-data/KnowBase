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

export interface BatchDocumentUploadItem {
  fileName: string;
  success: boolean;
  documentId: number | null;
  status: string | null;
  message: string;
}

export interface BatchDocumentUploadResponse {
  results: BatchDocumentUploadItem[];
}

export interface BatchDocumentDeleteItem {
  documentId: number;
  success: boolean;
  message: string;
}

export interface BatchDocumentDeleteResponse {
  results: BatchDocumentDeleteItem[];
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

export type QaStreamEvent =
  | {
      type: 'citations';
      citations: Citation[];
    }
  | {
      type: 'answer_delta';
      delta: string;
    }
  | {
      type: 'done';
      answer: string;
      citations: Citation[];
    }
  | {
      type: 'error';
      message: string;
    };

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
