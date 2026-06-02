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
import java.util.function.Consumer;
import java.util.stream.Stream;
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

    @Override
    public void streamAnswer(String question, List<RetrievedChunk> contexts, Consumer<String> onDelta) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }
        if (contexts == null || contexts.isEmpty()) {
            throw new IllegalArgumentException("Contexts must not be empty");
        }
        if (onDelta == null) {
            throw new IllegalArgumentException("Delta callback must not be null");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("DEEPSEEK_API_KEY must be configured");
        }

        try {
            HttpResponse<Stream<String>> response = httpClient.send(buildStreamRequest(question, contexts), HttpResponse.BodyHandlers.ofLines());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("DeepSeek chat completion stream request failed with status " + response.statusCode());
            }
            try (Stream<String> lines = response.body()) {
                lines.forEach(line -> handleStreamLine(line, onDelta));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to call DeepSeek chat completion stream API", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("DeepSeek chat completion stream request was interrupted", exception);
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
                new DeepSeekThinking("disabled"),
                false
        ));

        return HttpRequest.newBuilder()
                .uri(URI.create(normalizeBaseUrl(baseUrl) + CHAT_COMPLETIONS_PATH))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private HttpRequest buildStreamRequest(String question, List<RetrievedChunk> contexts) throws IOException {
        String body = objectMapper.writeValueAsString(new DeepSeekChatRequest(
                model,
                List.of(
                        new DeepSeekMessage("system", systemPrompt()),
                        new DeepSeekMessage("user", userPrompt(question, contexts))
                ),
                maxTokens,
                new DeepSeekThinking("disabled"),
                true
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

    private void handleStreamLine(String line, Consumer<String> onDelta) {
        if (line == null || line.isBlank() || line.startsWith(":")) {
            return;
        }
        if (!line.startsWith("data:")) {
            return;
        }

        String data = line.substring("data:".length()).trim();
        if ("[DONE]".equals(data)) {
            return;
        }

        try {
            JsonNode contentNode = objectMapper.readTree(data)
                    .path("choices")
                    .path(0)
                    .path("delta")
                    .path("content");
            if (contentNode.isTextual() && !contentNode.asText().isEmpty()) {
                onDelta.accept(contentNode.asText());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to parse DeepSeek stream response", exception);
        }
    }

    private String systemPrompt() {
        return """
                你是 KnowBase 的知识库问答助手。你必须只根据用户提供的知识库片段回答，不能编造知识库片段之外的事实。
                回答使用中文，结构要清晰。优先先给出结论，再分点展开说明。
                如果多个知识库片段都与问题相关，要综合归纳，不要只复述其中一个片段。
                如果知识库片段不足以回答问题，请明确说明缺少哪些信息。
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
        prompt.append("""
                请基于以上片段完整回答用户问题。回答要求：
                1. 先给出简短结论；
                2. 再分点展开说明；
                3. 如果多个片段相关，要综合归纳；
                4. 可以在要点中说明依据来自哪些片段编号；
                5. 不要使用知识库片段之外的信息。
                """);
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
        DeepSeekThinking thinking,
        boolean stream
    ) {
    }

    private record DeepSeekMessage(String role, String content) {
    }

    private record DeepSeekThinking(String type) {
    }
}
