package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.DocumentResponse;
import com.zhen.knowbase.dto.DocumentUploadResponse;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.entity.DocumentStatus;
import com.zhen.knowbase.repository.DocumentRepository;
import com.zhen.knowbase.service.FileStorageService.StoredFile;
import java.io.UncheckedIOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final ParserService parserService;

    public DocumentService(
            DocumentRepository documentRepository,
            FileStorageService fileStorageService,
            ParserService parserService
    ) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
        this.parserService = parserService;
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

    private void parseUploadedDocument(Document document) {
        try {
            parserService.parse(document.getFilePath());
            document.markParsing();
        } catch (IllegalArgumentException | UncheckedIOException exception) {
            document.markFailed();
        }
    }
}
