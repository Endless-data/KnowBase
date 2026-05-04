package com.zhen.knowbase.service;

import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.entity.DocumentStatus;
import com.zhen.knowbase.repository.ChunkRepository;
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
    private final ChunkService chunkService = new ChunkService(chunkRepository);

    @Test
    void splitsContentByFixedLengthAndSavesChunks() {
        Document document = new Document("note.md", "md", "/tmp/note.md", DocumentStatus.PARSING, 1001L);
        String content = "a".repeat(ChunkService.CHUNK_SIZE)
                + "b".repeat(ChunkService.CHUNK_SIZE)
                + "c";
        when(chunkRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Chunk> chunks = chunkService.createChunks(document, content);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).getChunkIndex()).isZero();
        assertThat(chunks.get(0).getContent()).hasSize(ChunkService.CHUNK_SIZE);
        assertThat(chunks.get(1).getChunkIndex()).isEqualTo(1);
        assertThat(chunks.get(1).getContent()).hasSize(ChunkService.CHUNK_SIZE);
        assertThat(chunks.get(2).getChunkIndex()).isEqualTo(2);
        assertThat(chunks.get(2).getContent()).isEqualTo("c");
        assertThat(chunks.get(2).getVectorId()).isNull();
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
        chunkService.deleteChunksByDocumentId(1L);

        verify(chunkRepository).deleteByDocumentId(1L);
    }
}
