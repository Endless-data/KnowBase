package com.zhen.knowbase.dto;

import com.zhen.knowbase.entity.ChatRecord;
import java.time.LocalDateTime;
import java.util.List;

public record HistoryDetailResponse(
        Long id,
        String question,
        String answer,
        Integer retrievalCount,
        LocalDateTime createdAt,
        List<CitationResponse> citations
) {

    public static HistoryDetailResponse from(ChatRecord chatRecord, List<CitationResponse> citations) {
        return new HistoryDetailResponse(
                chatRecord.getId(),
                chatRecord.getQuestion(),
                chatRecord.getAnswer(),
                chatRecord.getRetrievalCount(),
                chatRecord.getCreatedAt(),
                citations
        );
    }
}
