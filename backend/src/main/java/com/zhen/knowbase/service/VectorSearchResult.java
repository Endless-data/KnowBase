package com.zhen.knowbase.service;

public record VectorSearchResult(
        Long chunkId,
        double score
) {
}
