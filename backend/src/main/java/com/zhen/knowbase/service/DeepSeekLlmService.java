package com.zhen.knowbase.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhen.knowbase.dto.RetrievedChunk;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "knowbase.llm.provider", havingValue = "deepseek", matchIfMissing = true)
public class DeepSeekLlmService implements LlmService {

    static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final int maxTokens;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public DeepSeekLlmService(
            @Value("${DEEPSEEK_API_KEY:}") String apiKey,
            @Value("${knowbase.llm.model:deepseek-v4-flash}") String model,
            @Value("${knowbase.llm.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${knowbase.llm.max-tokens:1000}") int maxTokens,
            ObjectMapper objectMapper
    ) {
        this(apiKey, model, baseUrl, maxTokens, HttpClient.newHttpClient(), objectMapper);
    }

    DeepSeekLlmService(
            String apiKey,
            String model,
            String baseUrl,
            int maxTokens,
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl;
        this.maxTokens = maxTokens;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateAnswer(String question, List<RetrievedChunk> contexts) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }
        if (contexts == null || contexts.isEmpty()) {
            throw new IllegalArgumentException("Contexts must not be empty");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("DEEPSEEK_API_KEY must be configured");
        }

        try {
            HttpResponse<String> response = httpClient.send(buildRequest(question, contexts), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("DeepSeek chat completion request failed with status " + response.statusCode());
            }
            return parseAnswer(response.body());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to call DeepSeek chat completion API", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DeepSeek chat completion request was interrupted", exception);
        }
    }

    private HttpRequest buildRequest(String question, List<RetrievedChunk> contexts) throws IOException {
        String body = objectMapper.writeValueAsString(new DeepSeekChatRequest(
                model,
                List.of(
                        new DeepSeekMessage("system", systemPrompt()),
                        new DeepSeekMessage("user", userPrompt(question, contexts))
                ),
                maxTokens,
                new DeepSeekThinking("disabled")
        ));

        return HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl(baseUrl) + CHAT_COMPLETIONS_PATH))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private String parseAnswer(String responseBody) throws IOException {
        JsonNode contentNode = objectMapper.readTree(responseBody)
                .path("choices")
                .path(0)
                .path("message")
                .path("content");
        if (!contentNode.isTextual() || contentNode.asText().isBlank()) {
            throw new IllegalStateException("DeepSeek chat completion response does not contain answer");
        }
        return contentNode.asText().trim();
    }

    private String systemPrompt() {
        return """
                你是 KnowBase 的知识库问答助手。你必须只根据用户提供的知识库片段回答。
                如果知识库片段不足以回答问题，请直接说明知识库中没有足够信息。
                回答使用中文，简洁、直接，不要编造知识库片段之外的事实。
                """;
    }

    private String userPrompt(String question, List<RetrievedChunk> contexts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户问题：").append(question).append("\n\n");
        prompt.append("知识库片段：\n");
        for (int index = 0; index < contexts.size(); index++) {
            RetrievedChunk chunk = contexts.get(index);
            prompt.append("[")
                    .append(index + 1)
                    .append("] 文档：")
                    .append(chunk.documentName())
                    .append("，chunk #")
                    .append(chunk.chunkIndex())
                    .append("\n")
                    .append(chunk.content())
                    .append("\n\n");
        }
        prompt.append("请基于以上片段回答用户问题。");
        return prompt.toString();
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://api.deepseek.com";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record DeepSeekChatRequest(
            String model,
            List<DeepSeekMessage> messages,
            @JsonProperty("max_tokens") int maxTokens,
            DeepSeekThinking thinking
    ) {
    }

    private record DeepSeekMessage(String role, String content) {
    }

    private record DeepSeekThinking(String type) {
    }
}
