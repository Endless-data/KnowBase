package com.zhen.knowbase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentStatus status;

    private Long fileSize;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Document() {
    }

    public Document(String name, String fileType, String filePath, DocumentStatus status, Long fileSize) {
        this.name = name;
        this.fileType = fileType;
        this.filePath = filePath;
        this.status = status;
        this.fileSize = fileSize;
    }

    public void markParsing() {
        this.status = DocumentStatus.PARSING;
    }

    public void markFailed() {
        this.status = DocumentStatus.FAILED;
    }

    public void markIndexed() {
        this.status = DocumentStatus.INDEXED;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
