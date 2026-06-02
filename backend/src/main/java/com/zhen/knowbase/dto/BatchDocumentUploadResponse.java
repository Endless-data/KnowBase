package com.zhen.knowbase.dto;

import java.util.List;

public record BatchDocumentUploadResponse(List<BatchDocumentUploadItem> results) {
}
