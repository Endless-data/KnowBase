package com.zhen.knowbase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhen.knowbase.dto.RetrievedChunk;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeepSeekLlmServiceTests {

    private final HttpClient httpClient = mock(HttpClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void callsDeepSeekAndParsesAnswer() throws Exception {
        HttpResponse<String> response = mockResponse(200, """
                {"choices":[{"message":{"content":"KnowBase 是个人知识库系统。"}}]}
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        DeepSeekLlmService llmService = new DeepSeekLlmService(
                "test-key",
                "deepseek-v4-flash",
                "https://api.deepseek.com/",
                1000,
                httpClient,
                objectMapper
        );

        String answer = llmService.generateAnswer("KnowBase 是什么", List.of(context()));

        assertThat(answer).isEqualTo("KnowBase 是个人知识库系统。");
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest request = requestCaptor.getValue();
        assertThat(request.uri().toString()).isEqualTo("https://api.deepseek.com/chat/completions");
        assertThat(request.headers().firstValue("Authorization")).contains("Bearer test-key");
    }

    @Test
    void rejectsMissingApiKeyBeforeCallingDeepSeek() throws Exception {
        DeepSeekLlmService llmService = new DeepSeekLlmService(
                "",
                "deepseek-v4-flash",
                "https://api.deepseek.com",
                1000,
                httpClient,
                objectMapper
        );

        assertThatThrownBy(() -> llmService.generateAnswer("KnowBase 是什么", List.of(context())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("DEEPSEEK_API_KEY must be configured");
        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void rejectsEmptyContexts() {
        DeepSeekLlmService llmService = new DeepSeekLlmService(
                "test-key",
                "deepseek-v4-flash",
                "https://api.deepseek.com",
                1000,
                httpClient,
                objectMapper
        );

        assertThatThrownBy(() -> llmService.generateAnswer("KnowBase 是什么", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Contexts must not be empty");
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    private RetrievedChunk context() {
        return new RetrievedChunk(
                10L,
                1L,
                "README.md",
                0,
                "KnowBase 是个人知识库系统。",
                0.9
        );
    }
}
