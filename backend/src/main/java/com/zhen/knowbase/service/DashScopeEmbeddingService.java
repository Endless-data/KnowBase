package com.zhen.knowbase.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "knowbase.embedding.provider", havingValue = "dashscope", matchIfMissing = true)
public class DashScopeEmbeddingService implements EmbeddingService {

    static final String ENDPOINT = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";

    private final String apiKey;
    private final String model;
    private final int dimension;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public DashScopeEmbeddingService(
            @Value("${DASHSCOPE_API_KEY:}") String apiKey,
            @Value("${knowbase.embedding.model:text-embedding-v4}") String model,
            @Value("${knowbase.embedding.dimension:1024}") int dimension,
            ObjectMapper objectMapper
    ) {
        this(apiKey, model, dimension, HttpClient.newHttpClient(), objectMapper);
    }

    DashScopeEmbeddingService(
            String apiKey,
            String model,
            int dimension,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.dimension = dimension;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Float> embed(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text must not be empty");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("DASHSCOPE_API_KEY must be configured");
        }

        try {
            HttpResponse<String> response = httpClient.send(buildRequest(text), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("DashScope embedding request failed with status " + response.statusCode());
            }
            return parseEmbedding(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to call DashScope embedding API", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DashScope embedding request was interrupted", exception);
        }
    }

    private HttpRequest buildRequest(String text) throws IOException {
        String body = objectMapper.writeValueAsString(new DashScopeEmbeddingRequest(
                model,
                new DashScopeInput(List.of(text)),
                new DashScopeParameters(dimension, "dense")
        ));

        return HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private List<Float> parseEmbedding(String responseBody) throws IOException {
        JsonNode embeddingNode = objectMapper.readTree(responseBody)
                .path("output")
                .path("embeddings")
                .path(0)
                .path("embedding");
        if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
            throw new IllegalStateException("DashScope embedding response does not contain embedding");
        }
        return objectMapper.convertValue(embeddingNode, objectMapper.getTypeFactory().constructCollectionType(List.class, Float.class));
    }

    private record DashScopeEmbeddingRequest(
            String model,
            DashScopeInput input,
            DashScopeParameters parameters
    ) {
    }

    private record DashScopeInput(List<String> texts) {
    }

    private record DashScopeParameters(int dimension, @JsonProperty("output_type") String outputType) {
    }
}
