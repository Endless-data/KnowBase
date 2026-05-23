package com.zhen.knowbase.repository;

import com.zhen.knowbase.entity.Chunk;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    void deleteByDocumentId(Long documentId);

    List<Chunk> findByDocumentId(Long documentId);

    List<Chunk> findByContentContainingIgnoreCase(String keyword, Pageable pageable);
}
