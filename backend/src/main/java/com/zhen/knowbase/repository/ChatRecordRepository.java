package com.zhen.knowbase.repository;

import com.zhen.knowbase.entity.ChatRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {

    List<ChatRecord> findAllByOrderByCreatedAtDesc();
}
