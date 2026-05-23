package com.zhen.knowbase.service;

import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.entity.DocumentStatus;
import com.zhen.knowbase.repository.ChunkRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChunkServiceTests {

    private final ChunkRepository chunkRepository = mock(ChunkRepository.class);
    private final EmbeddingService embeddingService = mock(EmbeddingService.class);
    private final VectorStoreService vectorStoreService = mock(VectorStoreService.class);
    private final ChunkService chunkService = new ChunkService(chunkRepository, embeddingService, vectorStoreService);

    @Test
    void splitsContentByFixedLengthAndSavesChunksWithVectors() {
        Document document = new Document("note.md", "md", "/tmp/note.md", DocumentStatus.PARSING, 1001L);
        String content = "a".repeat(ChunkService.CHUNK_SIZE)
                + "b".repeat(ChunkService.CHUNK_SIZE)
                + "c";
        when(chunkRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Chunk> chunks = invocation.getArgument(0);
            for (int index = 0; index < chunks.size(); index++) {
                if (chunks.get(index).getId() == null) {
                    setId(chunks.get(index), 10L + index);
                }
            }
            return chunks;
        });
        when(embeddingService.embed("a".repeat(ChunkService.CHUNK_SIZE))).thenReturn(List.of(1F, 0F));
        when(embeddingService.embed("b".repeat(ChunkService.CHUNK_SIZE))).thenReturn(List.of(0F, 1F));
        when(embeddingService.embed("c")).thenReturn(List.of(0.5F, 0.5F));
        when(vectorStoreService.saveVector(10L, List.of(1F, 0F))).thenReturn("mysql-vector-10");
        when(vectorStoreService.saveVector(11L, List.of(0F, 1F))).thenReturn("mysql-vector-11");
        when(vectorStoreService.saveVector(12L, List.of(0.5F, 0.5F))).thenReturn("mysql-vector-12");

        List<Chunk> chunks = chunkService.createChunks(document, content);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).getChunkIndex()).isZero();
        assertThat(chunks.get(0).getContent()).hasSize(ChunkService.CHUNK_SIZE);
        assertThat(chunks.get(1).getChunkIndex()).isEqualTo(1);
        assertThat(chunks.get(1).getContent()).hasSize(ChunkService.CHUNK_SIZE);
        assertThat(chunks.get(2).getChunkIndex()).isEqualTo(2);
        assertThat(chunks.get(2).getContent()).isEqualTo("c");
        assertThat(chunks).extracting(Chunk::getVectorId)
                .containsExactly("mysql-vector-10", "mysql-vector-11", "mysql-vector-12");
    }

    @Test
    void rejectsBlankContent() {
        Document document = new Document("empty.txt", "txt", "/tmp/empty.txt", DocumentStatus.PARSING, 1L);

        assertThatThrownBy(() -> chunkService.createChunks(document, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Document content must not be empty");
    }

    @Test
    void deletesChunksByDocumentId() {
        Chunk first = new Chunk(new Document("note.md", "md", "/tmp/note.md", DocumentStatus.INDEXED, 100L), 0, "content", "mysql-vector-10");
        setId(first, 10L);
        when(chunkRepository.findByDocumentId(1L)).thenReturn(List.of(first));

        chunkService.deleteChunksByDocumentId(1L);

        verify(vectorStoreService).deleteVectorsByChunkIds(List.of(10L));
        verify(chunkRepository).deleteByDocumentId(1L);
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
