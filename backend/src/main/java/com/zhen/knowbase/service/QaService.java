package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.AskResponse;
import com.zhen.knowbase.dto.CitationResponse;
import com.zhen.knowbase.dto.RetrievedChunk;
import com.zhen.knowbase.entity.ChatRecord;
import com.zhen.knowbase.entity.Citation;
import com.zhen.knowbase.repository.ChatRecordRepository;
import com.zhen.knowbase.repository.CitationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QaService {

    private static final int RETRIEVAL_TOP_K = 3;

    private final RetrievalService retrievalService;
    private final LlmService llmService;
    private final ChatRecordRepository chatRecordRepository;
    private final CitationRepository citationRepository;

    public QaService(
            RetrievalService retrievalService,
            LlmService llmService,
            ChatRecordRepository chatRecordRepository,
            CitationRepository citationRepository
    ) {
        this.retrievalService = retrievalService;
        this.llmService = llmService;
        this.chatRecordRepository = chatRecordRepository;
        this.citationRepository = citationRepository;
    }

    @Transactional
    public AskResponse ask(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }

        List<RetrievedChunk> retrievedChunks = retrievalService.retrieve(question, RETRIEVAL_TOP_K);
        if (retrievedChunks.isEmpty()) {
            AskResponse response = new AskResponse("知识库中暂无相关内容", List.of());
            persistChatRecord(question, response);
            return response;
        }

        String answer = llmService.generateAnswer(question, retrievedChunks);
        List<CitationResponse> citations = retrievedChunks.stream()
                .map(CitationResponse::from)
                .toList();
        AskResponse response = new AskResponse(answer, citations);
        persistChatRecord(question, response);
        return response;
    }

    private void persistChatRecord(String question, AskResponse response) {
        ChatRecord chatRecord = chatRecordRepository.save(new ChatRecord(
                question,
                response.answer(),
                response.citations().size()
        ));
        List<Citation> citations = response.citations()
                .stream()
                .map(citation -> Citation.from(chatRecord, citation))
                .toList();
        citationRepository.saveAll(citations);
    }
}
