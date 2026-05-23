package com.zhen.knowbase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhen.knowbase.entity.ChunkVector;
import com.zhen.knowbase.repository.ChunkVectorRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "knowbase.vector-store.provider", havingValue = "mysql", matchIfMissing = true)
public class MySqlVectorStoreService implements VectorStoreService {

    private static final TypeReference<List<Float>> VECTOR_TYPE = new TypeReference<>() {
    };

    private final ChunkVectorRepository chunkVectorRepository;
    private final ObjectMapper objectMapper;

    public MySqlVectorStoreService(ChunkVectorRepository chunkVectorRepository, ObjectMapper objectMapper) {
        this.chunkVectorRepository = chunkVectorRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public String saveVector(Long chunkId, List<Float> vector) {
        if (chunkId == null) {
            throw new IllegalArgumentException("Chunk id must not be null");
        }
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("Vector must not be empty");
        }

        String vectorId = "mysql-vector-" + chunkId;
        chunkVectorRepository.deleteByVectorId(vectorId);
        chunkVectorRepository.save(new ChunkVector(vectorId, chunkId, toJson(vector), vector.size()));
        return vectorId;
    }

    @Override
    @Transactional
    public void deleteVector(String vectorId) {
        if (vectorId == null || vectorId.isBlank()) {
            return;
        }
        chunkVectorRepository.deleteByVectorId(vectorId);
    }

    @Override
    @Transactional
    public void deleteVectorsByChunkIds(List<Long> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            return;
        }
        chunkVectorRepository.deleteByChunkIdIn(chunkIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VectorSearchResult> searchSimilar(List<Float> queryVector, int topK) {
        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalArgumentException("Query vector must not be empty");
        }
        if (topK <= 0) {
            return List.of();
        }

        return chunkVectorRepository.findAll()
                .stream()
                .map(chunkVector -> new VectorSearchResult(
                        chunkVector.getChunkId(),
                        cosineSimilarity(queryVector, fromJson(chunkVector.getVectorJson()))
                ))
                .filter(result -> result.score() > 0)
                .sorted((left, right) -> Double.compare(right.score(), left.score()))
                .limit(topK)
                .toList();
    }

    private String toJson(List<Float> vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to serialize vector", exception);
        }
    }

    private List<Float> fromJson(String vectorJson) {
        try {
            return objectMapper.readValue(vectorJson, VECTOR_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to deserialize vector", exception);
        }
    }

    private double cosineSimilarity(List<Float> left, List<Float> right) {
        int dimension = Math.min(left.size(), right.size());
        if (dimension == 0) {
            return 0;
        }

        double dotProduct = 0;
        double leftMagnitude = 0;
        double rightMagnitude = 0;
        for (int index = 0; index < dimension; index++) {
            float leftValue = left.get(index);
            float rightValue = right.get(index);
            dotProduct += leftValue * rightValue;
            leftMagnitude += leftValue * leftValue;
            rightMagnitude += rightValue * rightValue;
        }
        if (leftMagnitude == 0 || rightMagnitude == 0) {
            return 0;
        }
        return dotProduct / (Math.sqrt(leftMagnitude) * Math.sqrt(rightMagnitude));
    }
}
