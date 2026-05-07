package com.zhen.knowbase.entity;

import com.zhen.knowbase.dto.CitationResponse;
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
@Table(name = "citation")
public class Citation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_record_id", nullable = false)
    private ChatRecord chatRecord;

    private Long chunkId;

    private Long documentId;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Citation() {
    }

    public Citation(
            ChatRecord chatRecord,
            Long chunkId,
            Long documentId,
            String documentName,
            Integer chunkIndex,
            String content
    ) {
        this.chatRecord = chatRecord;
        this.chunkId = chunkId;
        this.documentId = documentId;
        this.documentName = documentName;
        this.chunkIndex = chunkIndex;
        this.content = content;
    }

    public static Citation from(ChatRecord chatRecord, CitationResponse citation) {
        return new Citation(
                chatRecord,
                citation.chunkId(),
                citation.documentId(),
                citation.documentName(),
                citation.chunkIndex(),
                citation.content()
        );
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ChatRecord getChatRecord() {
        return chatRecord;
    }

    public Long getChunkId() {
        return chunkId;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
