package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.RetrievedChunk;
import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.entity.DocumentStatus;
import com.zhen.knowbase.repository.ChunkRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetrievalServiceTests {

    private final ChunkRepository chunkRepository = mock(ChunkRepository.class);
    private final EmbeddingService embeddingService = mock(EmbeddingService.class);
    private final VectorStoreService vectorStoreService = mock(VectorStoreService.class);
    private final RetrievalService retrievalService = new RetrievalService(chunkRepository, embeddingService, vectorStoreService);

    @Test
    void retrievesTopKChunksByVectorSimilarity() {
        Document document = document(1L, "README.md");
        Chunk lowScore = chunk(10L, document, 0, "Spring Boot service");
        Chunk highScore = chunk(11L, document, 1, "Spring Boot backend supports Spring retrieval");
        when(embeddingService.embed("spring backend")).thenReturn(List.of(1F, 0F));
        when(vectorStoreService.searchSimilar(List.of(1F, 0F), 1))
                .thenReturn(List.of(new VectorSearchResult(11L, 0.95)));
        when(chunkRepository.findAllById(List.of(11L))).thenReturn(List.of(highScore, lowScore));

        List<RetrievedChunk> results = retrievalService.retrieve("spring backend", 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).chunkId()).isEqualTo(11L);
        assertThat(results.get(0).documentId()).isEqualTo(1L);
        assertThat(results.get(0).documentName()).isEqualTo("README.md");
        assertThat(results.get(0).chunkIndex()).isEqualTo(1);
        assertThat(results.get(0).score()).isEqualTo(0.95);
    }

    @Test
    void rejectsBlankQuestion() {
        assertThatThrownBy(() -> retrievalService.retrieve(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question must not be empty");
    }

    private Document document(Long id, String name) {
        Document document = new Document(name, "md", "/tmp/" + name, DocumentStatus.INDEXED, 100L);
        setId(document, id);
        return document;
    }

    private Chunk chunk(Long id, Document document, Integer chunkIndex, String content) {
        Chunk chunk = new Chunk(document, chunkIndex, content, null);
        setId(chunk, id);
        return chunk;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set test entity id", exception);
        }
    }
}
