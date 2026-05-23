package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.DocumentResponse;
import com.zhen.knowbase.dto.DocumentUploadResponse;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.entity.DocumentStatus;
import com.zhen.knowbase.repository.DocumentRepository;
import com.zhen.knowbase.service.FileStorageService.StoredFile;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final ParserService parserService;
    private final ChunkService chunkService;

    public DocumentService(
            DocumentRepository documentRepository,
            FileStorageService fileStorageService,
            ParserService parserService,
            ChunkService chunkService
    ) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.parserService = parserService;
        this.chunkService = chunkService;
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @Transactional
    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        StoredFile storedFile = fileStorageService.store(file);
        Document document = new Document(
                storedFile.originalFilename(),
                storedFile.fileType(),
                storedFile.filePath(),
                DocumentStatus.UPLOADED,
                storedFile.fileSize()
        );

        Document savedDocument = documentRepository.save(document);
        parseUploadedDocument(savedDocument);
        return DocumentUploadResponse.from(savedDocument);
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        chunkService.deleteChunksByDocumentId(id);
        documentRepository.delete(document);
        fileStorageService.deleteStoredFile(document.getFilePath());
    }

    private void parseUploadedDocument(Document document) {
        try {
            String content = parserService.parse(document.getFilePath());
            chunkService.createChunks(document, content);
            document.markIndexed();
        } catch (RuntimeException exception) {
            document.markFailed();
        }
    }
}
