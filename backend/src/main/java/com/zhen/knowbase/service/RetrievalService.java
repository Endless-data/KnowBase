package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.RetrievedChunk;
import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.repository.ChunkRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetrievalService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;

    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public RetrievalService(
            ChunkRepository chunkRepository,
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService
    ) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    @Transactional(readOnly = true)
    public List<RetrievedChunk> retrieve(String question) {
        return retrieve(question, DEFAULT_TOP_K);
    }

    @Transactional(readOnly = true)
    public List<RetrievedChunk> retrieve(String question, int topK) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }

        int normalizedTopK = normalizeTopK(topK);
        List<VectorSearchResult> searchResults = vectorStoreService.searchSimilar(
                embeddingService.embed(question),
                normalizedTopK
        );
        if (searchResults.isEmpty()) {
            return List.of();
        }

        Map<Long, Chunk> chunksById = chunkRepository.findAllById(searchResults.stream()
                        .map(VectorSearchResult::chunkId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(Chunk::getId, Function.identity()));

        return searchResults.stream()
                .filter(result -> chunksById.containsKey(result.chunkId()))
                .map(result -> RetrievedChunk.from(chunksById.get(result.chunkId()), result.score()))
                .toList();
    }

    private int normalizeTopK(int topK) {
        if (topK <= 0) {
            return DEFAULT_TOP_K;
        }
        return Math.min(topK, MAX_TOP_K);
    }
}
