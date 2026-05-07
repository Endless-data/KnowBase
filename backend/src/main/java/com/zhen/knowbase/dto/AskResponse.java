package com.zhen.knowbase.dto;

import java.util.List;

public record AskResponse(
        String answer,
        List<CitationResponse> citations
) {
}
