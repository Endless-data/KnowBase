package com.zhen.knowbase.dto;

import com.zhen.knowbase.entity.Document;

public record DocumentUploadResponse(
        Long documentId,
        String name,
        String status
) {

    public static DocumentUploadResponse from(Document document) {
        return new DocumentUploadResponse(
                document.getId(),
                document.getName(),
                document.getStatus().name()
        );
    }
}
