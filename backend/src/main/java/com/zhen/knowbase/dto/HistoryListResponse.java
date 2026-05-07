package com.zhen.knowbase.dto;

import com.zhen.knowbase.entity.ChatRecord;
import java.time.LocalDateTime;

public record HistoryListResponse(
        Long id,
        String question,
        String answer,
        Integer retrievalCount,
        LocalDateTime createdAt
) {

    public static HistoryListResponse from(ChatRecord chatRecord) {
        return new HistoryListResponse(
                chatRecord.getId(),
                chatRecord.getQuestion(),
                chatRecord.getAnswer(),
                chatRecord.getRetrievalCount(),
                chatRecord.getCreatedAt()
        );
    }
}
