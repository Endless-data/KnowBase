package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.RetrievedChunk;
import java.util.List;

public interface LlmService {

    String generateAnswer(String question, List<RetrievedChunk> contexts);
}
