package com.zhen.knowbase.repository;

import com.zhen.knowbase.entity.ChunkVector;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChunkVectorRepository extends JpaRepository<ChunkVector, Long> {

    void deleteByVectorId(String vectorId);

    void deleteByChunkIdIn(Collection<Long> chunkIds);
}
