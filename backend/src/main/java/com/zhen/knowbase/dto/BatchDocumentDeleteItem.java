package com.zhen.knowbase.dto;

public record BatchDocumentDeleteItem(
        Long documentId,
        boolean success,
        String message
) {

    public static BatchDocumentDeleteItem success(Long documentId) {
        return new BatchDocumentDeleteItem(documentId, true, "删除成功");
    }

    public static BatchDocumentDeleteItem failed(Long documentId, String message) {
        return new BatchDocumentDeleteItem(documentId, false, message);
    }
}
