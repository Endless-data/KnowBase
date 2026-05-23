package com.zhen.knowbase.service;

import java.util.List;

public interface EmbeddingService {

    List<Float> embed(String text);
}
