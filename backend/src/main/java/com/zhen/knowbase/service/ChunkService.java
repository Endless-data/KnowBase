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

    public ChunkService(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public List<Chunk> createChunks(Document document, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Document content must not be empty");
        }

        List<Chunk> chunks = split(document, content);
        return chunkRepository.saveAll(chunks);
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
