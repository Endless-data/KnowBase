package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.AskResponse;
import com.zhen.knowbase.dto.CitationResponse;
import com.zhen.knowbase.dto.RetrievedChunk;
import com.zhen.knowbase.entity.ChatRecord;
import com.zhen.knowbase.entity.Citation;
import com.zhen.knowbase.repository.ChatRecordRepository;
import com.zhen.knowbase.repository.CitationRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class QaService {

    private static final int RETRIEVAL_TOP_K = 5;

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

    public SseEmitter askStream(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }

        SseEmitter emitter = new SseEmitter(300_000L);
        CompletableFuture.runAsync(() -> streamAnswer(question, emitter));
        return emitter;
    }

    private void streamAnswer(String question, SseEmitter emitter) {
        try {
            List<RetrievedChunk> retrievedChunks = retrievalService.retrieve(question, RETRIEVAL_TOP_K);
            List<CitationResponse> citations = retrievedChunks.stream()
                    .map(CitationResponse::from)
                    .toList();
            send(emitter, Map.of("type", "citations", "citations", citations));

            if (retrievedChunks.isEmpty()) {
                AskResponse response = new AskResponse("知识库中暂无相关内容", List.of());
                send(emitter, Map.of("type", "answer_delta", "delta", response.answer()));
                persistChatRecord(question, response);
                send(emitter, Map.of("type", "done", "answer", response.answer(), "citations", response.citations()));
                emitter.complete();
                return;
            }

            StringBuilder answerBuilder = new StringBuilder();
            llmService.streamAnswer(question, retrievedChunks, delta -> {
                answerBuilder.append(delta);
                send(emitter, Map.of("type", "answer_delta", "delta", delta));
            });

            AskResponse response = new AskResponse(answerBuilder.toString(), citations);
            persistChatRecord(question, response);
            send(emitter, Map.of("type", "done", "answer", response.answer(), "citations", response.citations()));
            emitter.complete();
        } catch (RuntimeException exception) {
            send(emitter, Map.of("type", "error", "message", exception.getMessage()));
            emitter.complete();
        }
    }

    private void send(SseEmitter emitter, Object data) {
        try {
            emitter.send(data);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to send stream response", exception);
        }
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
