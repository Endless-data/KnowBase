package com.zhen.knowbase.repository;

import com.zhen.knowbase.entity.Citation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitationRepository extends JpaRepository<Citation, Long> {
}
