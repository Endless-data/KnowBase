package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.RetrievedChunk;
import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.repository.ChunkRepository;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class RetrievalService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;
    private static final int CANDIDATE_MULTIPLIER = 5;

    private final ChunkRepository chunkRepository;

    public RetrievalService(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public List<RetrievedChunk> retrieve(String question) {
        return retrieve(question, DEFAULT_TOP_K);
    }

    public List<RetrievedChunk> retrieve(String question, int topK) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }

        int normalizedTopK = normalizeTopK(topK);
        List<String> keywords = extractKeywords(question);
        if (keywords.isEmpty()) {
            return List.of();
        }

        List<Chunk> candidates = chunkRepository.findByContentContainingIgnoreCase(
                keywords.get(0),
                PageRequest.of(0, normalizedTopK * CANDIDATE_MULTIPLIER)
        );

        return candidates.stream()
                .map(chunk -> RetrievedChunk.from(chunk, score(chunk, keywords)))
                .filter(retrievedChunk -> retrievedChunk.score() > 0)
                .sorted(Comparator.comparingInt(RetrievedChunk::score).reversed())
                .limit(normalizedTopK)
                .toList();
    }

    private int normalizeTopK(int topK) {
        if (topK <= 0) {
            return DEFAULT_TOP_K;
        }
        return Math.min(topK, MAX_TOP_K);
    }

    private List<String> extractKeywords(String question) {
        return Arrays.stream(question.trim().toLowerCase(Locale.ROOT).split("\\s+"))
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .toList();
    }

    private int score(Chunk chunk, List<String> keywords) {
        String content = chunk.getContent().toLowerCase(Locale.ROOT);
        int score = 0;
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                score++;
            }
        }
        return score;
    }
}
