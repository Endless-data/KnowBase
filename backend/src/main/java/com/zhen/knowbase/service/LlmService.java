package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.RetrievedChunk;
import java.util.List;
import java.util.function.Consumer;

public interface LlmService {

    String generateAnswer(String question, List<RetrievedChunk> contexts);

    void streamAnswer(String question, List<RetrievedChunk> contexts, Consumer<String> onDelta);
}
