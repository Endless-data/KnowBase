package com.zhen.knowbase.dto;

import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.entity.Document;

public record RetrievedChunk(
        Long chunkId,
        Long documentId,
        String documentName,
        Integer chunkIndex,
        String content,
        double score
) {

    public static RetrievedChunk from(Chunk chunk, double score) {
        Document document = chunk.getDocument();
        return new RetrievedChunk(
                chunk.getId(),
                document.getId(),
                document.getName(),
                chunk.getChunkIndex(),
                chunk.getContent(),
                score
        );
    }
}
