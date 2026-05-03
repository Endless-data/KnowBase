package com.zhen.knowbase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "chunk")
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String vectorId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Chunk() {
    }

    public Chunk(Document document, Integer chunkIndex, String content, String vectorId) {
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.vectorId = vectorId;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public String getVectorId() {
        return vectorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
