package com.zhen.knowbase.repository;

import com.zhen.knowbase.entity.Citation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitationRepository extends JpaRepository<Citation, Long> {

    List<Citation> findByChatRecordIdOrderByChunkIndexAsc(Long chatRecordId);
}
