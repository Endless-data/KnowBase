package com.zhen.knowbase.service;

import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.repository.ChunkRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ChunkService {

    static final int CHUNK_SIZE = 500;

    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public ChunkService(
            ChunkRepository chunkRepository,
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService
    ) {
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    public List<Chunk> createChunks(Document document, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Document content must not be empty");
        }

        List<Chunk> savedChunks = chunkRepository.saveAll(split(document, content));
        for (Chunk chunk : savedChunks) {
            List<Float> vector = embeddingService.embed(chunk.getContent());
            String vectorId = vectorStoreService.saveVector(chunk.getId(), vector);
            chunk.setVectorId(vectorId);
        }
        return chunkRepository.saveAll(savedChunks);
    }

    public void deleteChunksByDocumentId(Long documentId) {
        List<Long> chunkIds = chunkRepository.findByDocumentId(documentId)
                .stream()
                .map(Chunk::getId)
                .toList();
        vectorStoreService.deleteVectorsByChunkIds(chunkIds);
        chunkRepository.deleteByDocumentId(documentId);
    }

    private List<Chunk> split(Document document, String content) {
        List<Chunk> chunks = new ArrayList<>();
        int chunkIndex = 0;
        for (int start = 0; start < content.length(); start += CHUNK_SIZE) {
            int end = Math.min(start + CHUNK_SIZE, content.length());
            chunks.add(new Chunk(document, chunkIndex, content.substring(start, end), null));
            chunkIndex++;
        }
        return chunks;
    }
}
