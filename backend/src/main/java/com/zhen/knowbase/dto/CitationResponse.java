package com.zhen.knowbase.dto;

public record CitationResponse(
        Long chunkId,
        Long documentId,
        String documentName,
        Integer chunkIndex,
        String content
) {

    public static CitationResponse from(RetrievedChunk chunk) {
        return new CitationResponse(
                chunk.chunkId(),
                chunk.documentId(),
                chunk.documentName(),
                chunk.chunkIndex(),
                chunk.content()
        );
    }
}
