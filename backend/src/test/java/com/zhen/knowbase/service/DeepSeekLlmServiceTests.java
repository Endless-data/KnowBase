package com.zhen.knowbase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhen.knowbase.dto.RetrievedChunk;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Stream;
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
        String requestBody = readRequestBody(request);
        assertThat(requestBody)
                .contains("先给出结论")
                .contains("分点展开说明")
                .contains("综合归纳")
                .contains("不要使用知识库片段之外的信息");
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
    void streamsDeepSeekAnswerDeltas() throws Exception {
        HttpResponse<Stream<String>> response = mockStreamResponse(200, Stream.of(
                "data: {\"choices\":[{\"delta\":{\"content\":\"KnowBase\"}}]}",
                "data: {\"choices\":[{\"delta\":{\"content\":\" 是个人知识库系统。\"}}]}",
                "data: [DONE]"
        ));
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);
        DeepSeekLlmService llmService = new DeepSeekLlmService(
                "test-key",
                "deepseek-v4-flash",
                "https://api.deepseek.com/",
                1000,
                httpClient,
                objectMapper
        );
        List<String> deltas = new ArrayList<>();

        llmService.streamAnswer("KnowBase 是什么", List.of(context()), deltas::add);

        assertThat(String.join("", deltas)).isEqualTo("KnowBase 是个人知识库系统。");
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

    @SuppressWarnings("unchecked")
    private HttpResponse<Stream<String>> mockStreamResponse(int statusCode, Stream<String> body) {
        HttpResponse<Stream<String>> response = mock(HttpResponse.class);
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

    private String readRequestBody(HttpRequest request) {
        CapturingSubscriber subscriber = new CapturingSubscriber();
        request.bodyPublisher()
                .orElseThrow(() -> new IllegalStateException("Request body publisher is missing"))
                .subscribe(subscriber);
        return subscriber.body();
    }

    private static final class CapturingSubscriber implements Flow.Subscriber<ByteBuffer> {

        private final StringBuilder body = new StringBuilder();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(ByteBuffer item) {
            body.append(StandardCharsets.UTF_8.decode(item));
        }

        @Override
        public void onError(Throwable throwable) {
            throw new IllegalStateException("Failed to read request body", throwable);
        }

        @Override
        public void onComplete() {
        }

        private String body() {
            return body.toString();
        }
    }
}
