package com.zhen.knowbase.service;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalEmbeddingServiceTests {

    private final LocalEmbeddingService embeddingService = new LocalEmbeddingService();

    @Test
    void embedsTextIntoStableVector() {
        List<Float> first = embeddingService.embed("KnowBase RAG");
        List<Float> second = embeddingService.embed("KnowBase RAG");

        assertThat(first).hasSize(LocalEmbeddingService.VECTOR_DIMENSION);
        assertThat(first).isEqualTo(second);
        assertThat(first).anySatisfy(value -> assertThat(value).isGreaterThan(0));
    }

    @Test
    void rejectsBlankText() {
        assertThatThrownBy(() -> embeddingService.embed(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Text must not be empty");
    }
}
