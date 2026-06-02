package com.zhen.knowbase.dto;

import java.util.List;

public record BatchDocumentDeleteRequest(List<Long> documentIds) {
}
