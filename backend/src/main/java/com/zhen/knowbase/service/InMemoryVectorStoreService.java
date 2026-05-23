package com.zhen.knowbase.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
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
}
