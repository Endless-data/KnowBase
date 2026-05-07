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
@Table(name = "chat_record")
public class ChatRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(nullable = false)
    private Integer retrievalCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ChatRecord() {
    }

    public ChatRecord(String question, String answer, Integer retrievalCount) {
        this.question = question;
        this.answer = answer;
        this.retrievalCount = retrievalCount;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public Integer getRetrievalCount() {
        return retrievalCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
