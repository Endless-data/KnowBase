package com.zhen.knowbase.service;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryVectorStoreServiceTests {

    private final InMemoryVectorStoreService vectorStoreService = new InMemoryVectorStoreService();

    @Test
    void savesVectorAndReturnsLocalVectorId() {
        String vectorId = vectorStoreService.saveVector(10L, List.of(0.1F, 0.2F, 0.3F));

        assertThat(vectorId).isEqualTo("local-vector-10");
    }

    @Test
    void deletesMissingVectorWithoutError() {
        vectorStoreService.deleteVector("missing-vector");
        vectorStoreService.deleteVector(null);
        vectorStoreService.deleteVector(" ");
    }

    @Test
    void rejectsInvalidVectorInput() {
        assertThatThrownBy(() -> vectorStoreService.saveVector(null, List.of(0.1F)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chunk id must not be null");
        assertThatThrownBy(() -> vectorStoreService.saveVector(10L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Vector must not be empty");
    }
}
