package com.zhen.knowbase.service;

import java.util.List;

public interface VectorStoreService {

    String saveVector(Long chunkId, List<Float> vector);

    void deleteVector(String vectorId);
}
