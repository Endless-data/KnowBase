package com.zhen.knowbase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhen.knowbase.entity.ChunkVector;
import com.zhen.knowbase.repository.ChunkVectorRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MySqlVectorStoreServiceTests {

    private final ChunkVectorRepository chunkVectorRepository = mock(ChunkVectorRepository.class);
    private final MySqlVectorStoreService vectorStoreService = new MySqlVectorStoreService(
            chunkVectorRepository,
            new ObjectMapper()
    );

    @Test
    void savesVectorAsJsonAndReturnsVectorId() {
        String vectorId = vectorStoreService.saveVector(10L, List.of(0.1F, 0.2F));

        assertThat(vectorId).isEqualTo("mysql-vector-10");
        ArgumentCaptor<ChunkVector> captor = ArgumentCaptor.forClass(ChunkVector.class);
        verify(chunkVectorRepository).save(captor.capture());
        ChunkVector saved = captor.getValue();
        assertThat(saved.getChunkId()).isEqualTo(10L);
        assertThat(saved.getDimension()).isEqualTo(2);
        assertThat(saved.getVectorJson()).contains("0.1");
    }

    @Test
    void searchesSimilarVectorsByCosineScore() {
        when(chunkVectorRepository.findAll()).thenReturn(List.of(
                new ChunkVector("mysql-vector-10", 10L, "[1.0,0.0]", 2),
                new ChunkVector("mysql-vector-11", 11L, "[0.5,0.5]", 2)
        ));

        List<VectorSearchResult> results = vectorStoreService.searchSimilar(List.of(1F, 0F), 2);

        assertThat(results).extracting(VectorSearchResult::chunkId).containsExactly(10L, 11L);
        assertThat(results.get(0).score()).isEqualTo(1.0);
    }

    @Test
    void rejectsInvalidVectorInput() {
        assertThatThrownBy(() -> vectorStoreService.saveVector(null, List.of(0.1F)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Chunk id must not be null");
        assertThatThrownBy(() -> vectorStoreService.searchSimilar(List.of(), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Query vector must not be empty");
    }
}
