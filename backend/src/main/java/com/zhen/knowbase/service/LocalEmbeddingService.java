package com.zhen.knowbase.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "knowbase.embedding.provider", havingValue = "local")
public class LocalEmbeddingService implements EmbeddingService {

    static final int VECTOR_DIMENSION = 16;

    @Override
    public List<Float> embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be empty");
        }

        float[] vector = new float[VECTOR_DIMENSION];
        byte[] bytes = text.trim().toLowerCase().getBytes(StandardCharsets.UTF_8);
        for (int index = 0; index < bytes.length; index++) {
            int bucket = index % VECTOR_DIMENSION;
            vector[bucket] += Byte.toUnsignedInt(bytes[index]);
        }

        return normalize(vector);
    }

    private List<Float> normalize(float[] vector) {
        float magnitude = 0;
        for (float value : vector) {
            magnitude += value * value;
        }

        if (magnitude == 0) {
            return List.of();
        }

        float scale = (float) Math.sqrt(magnitude);
        List<Float> normalized = new ArrayList<>(VECTOR_DIMENSION);
        for (float value : vector) {
            normalized.add(value / scale);
        }
        return normalized;
    }
}
