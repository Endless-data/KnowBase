package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.AskResponse;
import com.zhen.knowbase.dto.CitationResponse;
import com.zhen.knowbase.dto.RetrievedChunk;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QaService {

    private static final int RETRIEVAL_TOP_K = 3;

    private final RetrievalService retrievalService;

    public QaService(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    public AskResponse ask(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }

        List<RetrievedChunk> retrievedChunks = retrievalService.retrieve(question, RETRIEVAL_TOP_K);
        if (retrievedChunks.isEmpty()) {
            return new AskResponse("知识库中暂无相关内容", List.of());
        }

        String answer = buildAnswer(retrievedChunks);
        List<CitationResponse> citations = retrievedChunks.stream()
                .map(CitationResponse::from)
                .toList();
        return new AskResponse(answer, citations);
    }

    private String buildAnswer(List<RetrievedChunk> chunks) {
        StringBuilder answer = new StringBuilder("根据知识库内容，相关信息如下：");
        for (RetrievedChunk chunk : chunks) {
            answer.append("\n\n")
                    .append(chunk.content());
        }
        return answer.toString();
    }
}
