package com.zhen.knowbase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

class DashScopeEmbeddingServiceTests {

    private final HttpClient httpClient = mock(HttpClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void callsDashScopeAndParsesEmbedding() throws Exception {
        HttpResponse<String> response = mockResponse(200, """
                {"output":{"embeddings":[{"embedding":[0.1,0.2,0.3]}]}}
                """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        DashScopeEmbeddingService embeddingService = new DashScopeEmbeddingService(
                "test-key",
                "text-embedding-v4",
                1024,
                httpClient,
                objectMapper
        );

        List<Float> embedding = embeddingService.embed("KnowBase");

        assertThat(embedding).containsExactly(0.1F, 0.2F, 0.3F);
        ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
        HttpRequest request = requestCaptor.getValue();
        assertThat(request.uri().toString()).isEqualTo(DashScopeEmbeddingService.ENDPOINT);
        assertThat(request.headers().firstValue("Authorization")).contains("Bearer test-key");
    }

    @Test
    void rejectsMissingApiKeyBeforeCallingDashScope() throws Exception {
        DashScopeEmbeddingService embeddingService = new DashScopeEmbeddingService(
                "",
                "text-embedding-v4",
                1024,
                httpClient,
                objectMapper
        );

        assertThatThrownBy(() -> embeddingService.embed("KnowBase"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("DASHSCOPE_API_KEY must be configured");
        verify(httpClient, never()).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }
}
