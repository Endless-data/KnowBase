package com.zhen.knowbase.controller;

import com.zhen.knowbase.common.ApiResponse;
import com.zhen.knowbase.dto.BatchDocumentDeleteItem;
import com.zhen.knowbase.dto.BatchDocumentDeleteRequest;
import com.zhen.knowbase.dto.BatchDocumentDeleteResponse;
import com.zhen.knowbase.dto.BatchDocumentUploadItem;
import com.zhen.knowbase.dto.BatchDocumentUploadResponse;
import com.zhen.knowbase.dto.DocumentResponse;
import com.zhen.knowbase.dto.DocumentUploadResponse;
import com.zhen.knowbase.service.DocumentService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public ApiResponse<List<DocumentResponse>> listDocuments() {
        return ApiResponse.success(documentService.listDocuments());
    }

    @PostMapping("/upload")
    public ApiResponse<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success("上传成功", documentService.uploadDocument(file));
    }

    @PostMapping("/batch-upload")
    public ApiResponse<BatchDocumentUploadResponse> batchUploadDocuments(@RequestParam("files") MultipartFile[] files) {
        List<BatchDocumentUploadItem> results = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                results.add(BatchDocumentUploadItem.success(documentService.uploadDocument(file)));
            } catch (RuntimeException exception) {
                results.add(BatchDocumentUploadItem.failed(resolveFileName(file), exception.getMessage()));
            }
        }
        return ApiResponse.success("批量上传完成", new BatchDocumentUploadResponse(results));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/batch-delete")
    public ApiResponse<BatchDocumentDeleteResponse> batchDeleteDocuments(@RequestBody BatchDocumentDeleteRequest request) {
        List<Long> documentIds = request.documentIds() == null ? List.of() : request.documentIds();
        List<BatchDocumentDeleteItem> results = new ArrayList<>();
        for (Long documentId : documentIds) {
            try {
                documentService.deleteDocument(documentId);
                results.add(BatchDocumentDeleteItem.success(documentId));
            } catch (RuntimeException exception) {
                results.add(BatchDocumentDeleteItem.failed(documentId, exception.getMessage()));
            }
        }
        return ApiResponse.success("批量删除完成", new BatchDocumentDeleteResponse(results));
    }

    private String resolveFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "unknown";
        }
        return originalFilename;
    }
}
