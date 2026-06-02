package com.zhen.knowbase.dto;

public record BatchDocumentUploadItem(
        String fileName,
        boolean success,
        Long documentId,
        String status,
        String message
) {

    public static BatchDocumentUploadItem success(DocumentUploadResponse response) {
        return new BatchDocumentUploadItem(
                response.name(),
                true,
                response.documentId(),
                response.status(),
                "上传成功"
        );
    }

    public static BatchDocumentUploadItem failed(String fileName, String message) {
        return new BatchDocumentUploadItem(fileName, false, null, null, message);
    }
}
