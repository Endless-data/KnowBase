package com.zhen.knowbase.dto;

import com.zhen.knowbase.entity.Document;
import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        String name,
        String fileType,
        String status,
        LocalDateTime createdAt
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getName(),
                document.getFileType(),
                document.getStatus().name(),
                document.getCreatedAt()
        );
    }
}
