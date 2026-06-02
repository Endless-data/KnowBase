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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChunkServiceTests {

    private final ChunkRepository chunkRepository = mock(ChunkRepository.class);
    private final EmbeddingService embeddingService = mock(EmbeddingService.class);
    private final VectorStoreService vectorStoreService = mock(VectorStoreService.class);
    private final ChunkService chunkService = new ChunkService(
            chunkRepository,
            embeddingService,
            vectorStoreService,
            ChunkService.DEFAULT_CHUNK_SIZE,
            ChunkService.DEFAULT_CHUNK_OVERLAP
    );

    @Test
    void createsSingleChunkForShortContentAndSavesVector() {
        Document document = new Document("note.md", "md", "/tmp/note.md", DocumentStatus.PARSING, 1001L);
        String content = "第一段内容。\n\n第二段内容。";
        when(chunkRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Chunk> chunks = invocation.getArgument(0);
            for (int index = 0; index < chunks.size(); index++) {
                if (chunks.get(index).getId() == null) {
                    setId(chunks.get(index), 10L + index);
                }
            }
            return chunks;
        });
        when(embeddingService.embed(anyString())).thenReturn(List.of(1F, 0F));
        when(vectorStoreService.saveVector(anyLong(), anyList())).thenAnswer(invocation -> "mysql-vector-" + invocation.getArgument(0));

        List<Chunk> chunks = chunkService.createChunks(document, content);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getChunkIndex()).isZero();
        assertThat(chunks.get(0).getContent()).isEqualTo(content);
        assertThat(chunks).extracting(Chunk::getVectorId)
                .containsExactly("mysql-vector-10");
    }

    @Test
    void prefersSentenceBoundariesAndAddsOverlap() {
        ChunkService smallChunkService = new ChunkService(chunkRepository, embeddingService, vectorStoreService, 28, 12);
        Document document = new Document("note.md", "md", "/tmp/note.md", DocumentStatus.PARSING, 1001L);
        String content = "第一句话说明项目背景。第二句话说明主要目标。第三句话说明实现方式。";
        when(chunkRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Chunk> chunks = invocation.getArgument(0);
            for (int index = 0; index < chunks.size(); index++) {
                if (chunks.get(index).getId() == null) {
                    setId(chunks.get(index), 20L + index);
                }
            }
            return chunks;
        });
        when(embeddingService.embed(anyString())).thenReturn(List.of(1F, 0F));
        when(vectorStoreService.saveVector(anyLong(), anyList())).thenAnswer(invocation -> "mysql-vector-" + invocation.getArgument(0));

        List<Chunk> chunks = smallChunkService.createChunks(document, content);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).getContent()).endsWith("主要目标。");
        assertThat(chunks.get(1).getContent()).startsWith("第二句话说明主要目标。");
        assertThat(chunks).extracting(Chunk::getChunkIndex).containsExactly(0, 1);
    }

    @Test
    void hardSplitsLongTextWithoutSentenceBoundaries() {
        ChunkService smallChunkService = new ChunkService(chunkRepository, embeddingService, vectorStoreService, 10, 0);
        Document document = new Document("note.md", "md", "/tmp/note.md", DocumentStatus.PARSING, 1001L);
        String content = "a".repeat(25);
        when(chunkRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.embed(anyString())).thenReturn(List.of(1F, 0F));
        when(vectorStoreService.saveVector(anyLong(), anyList())).thenReturn("mysql-vector-null");

        List<Chunk> chunks = smallChunkService.createChunks(document, content);

        assertThat(chunks).extracting(Chunk::getContent)
                .containsExactly("a".repeat(10), "a".repeat(10), "a".repeat(5));
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
