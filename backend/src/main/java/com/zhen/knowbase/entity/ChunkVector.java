package com.zhen.knowbase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "chunk_vector")
public class ChunkVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String vectorId;

    @Column(nullable = false)
    private Long chunkId;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String vectorJson;

    @Column(nullable = false)
    private Integer dimension;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ChunkVector() {
    }

    public ChunkVector(String vectorId, Long chunkId, String vectorJson, Integer dimension) {
        this.vectorId = vectorId;
        this.chunkId = chunkId;
        this.vectorJson = vectorJson;
        this.dimension = dimension;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public String getVectorId() {
        return vectorId;
    }

    public Long getChunkId() {
        return chunkId;
    }

    public String getVectorJson() {
        return vectorJson;
    }

    public Integer getDimension() {
        return dimension;
    }
}
