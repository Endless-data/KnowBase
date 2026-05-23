package com.zhen.knowbase.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "knowbase.vector-store.provider", havingValue = "memory")
public class InMemoryVectorStoreService implements VectorStoreService {

    private final Map<String, List<Float>> vectors = new ConcurrentHashMap<>();

    @Override
    public String saveVector(Long chunkId, List<Float> vector) {
        if (chunkId == null) {
            throw new IllegalArgumentException("Chunk id must not be null");
        }
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("Vector must not be empty");
        }

        String vectorId = "local-vector-" + chunkId;
        vectors.put(vectorId, List.copyOf(vector));
        return vectorId;
    }

    @Override
    public void deleteVector(String vectorId) {
        if (vectorId == null || vectorId.isBlank()) {
            return;
        }
        vectors.remove(vectorId);
    }

    @Override
    public void deleteVectorsByChunkIds(List<Long> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            return;
        }
        chunkIds.stream()
                .map(chunkId -> "local-vector-" + chunkId)
                .forEach(vectors::remove);
    }

    @Override
    public List<VectorSearchResult> searchSimilar(List<Float> queryVector, int topK) {
        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalArgumentException("Query vector must not be empty");
        }
        if (topK <= 0) {
            return List.of();
        }

        return vectors.entrySet()
                .stream()
                .map(entry -> new VectorSearchResult(parseChunkId(entry.getKey()), cosineSimilarity(queryVector, entry.getValue())))
                .filter(result -> result.chunkId() != null)
                .sorted((left, right) -> Double.compare(right.score(), left.score()))
                .limit(topK)
                .toList();
    }

    private Long parseChunkId(String vectorId) {
        try {
            return Long.parseLong(vectorId.replace("local-vector-", ""));
        } catch (NumberFormatException exception) {
            return null;
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
