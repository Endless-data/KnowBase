package com.zhen.knowbase.repository;

import com.zhen.knowbase.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    void deleteByDocumentId(Long documentId);
}
