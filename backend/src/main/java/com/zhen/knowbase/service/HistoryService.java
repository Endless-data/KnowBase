package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.CitationResponse;
import com.zhen.knowbase.dto.HistoryDetailResponse;
import com.zhen.knowbase.dto.HistoryListResponse;
import com.zhen.knowbase.entity.ChatRecord;
import com.zhen.knowbase.entity.Citation;
import com.zhen.knowbase.repository.ChatRecordRepository;
import com.zhen.knowbase.repository.CitationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoryService {

    private final ChatRecordRepository chatRecordRepository;
    private final CitationRepository citationRepository;

    public HistoryService(ChatRecordRepository chatRecordRepository, CitationRepository citationRepository) {
        this.chatRecordRepository = chatRecordRepository;
        this.citationRepository = citationRepository;
    }

    @Transactional(readOnly = true)
    public List<HistoryListResponse> listHistory() {
        return chatRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(HistoryListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoryDetailResponse getHistory(Long id) {
        ChatRecord chatRecord = chatRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("History record not found"));
        List<CitationResponse> citations = citationRepository.findByChatRecordIdOrderByChunkIndexAsc(id)
                .stream()
                .map(this::toCitationResponse)
                .toList();
        return HistoryDetailResponse.from(chatRecord, citations);
    }

    private CitationResponse toCitationResponse(Citation citation) {
        return new CitationResponse(
                citation.getChunkId(),
                citation.getDocumentId(),
                citation.getDocumentName(),
                citation.getChunkIndex(),
                citation.getContent()
        );
    }
}
