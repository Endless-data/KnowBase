package com.zhen.knowbase.dto;

import java.util.List;

public record BatchDocumentDeleteResponse(List<BatchDocumentDeleteItem> results) {
}
