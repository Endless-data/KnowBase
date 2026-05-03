package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.DocumentUploadResponse;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.repository.DocumentRepository;
import com.zhen.knowbase.service.FileStorageService.StoredFile;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentServiceTests {

    private final DocumentRepository documentRepository = mock(DocumentRepository.class);
    private final FileStorageService fileStorageService = mock(FileStorageService.class);
    private final ParserService parserService = mock(ParserService.class);
    private final ChunkService chunkService = mock(ChunkService.class);
    private final DocumentService documentService = new DocumentService(
            documentRepository,
            fileStorageService,
            parserService,
            chunkService
    );

    @Test
    void marksUploadedDocumentAsIndexedWhenParseAndChunkSucceed() {
        MockMultipartFile file = new MockMultipartFile("file", "note.md", "text/markdown", "# title".getBytes());
        when(fileStorageService.store(file)).thenReturn(new StoredFile("note.md", "md", "/tmp/note.md", 7L));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parserService.parse("/tmp/note.md")).thenReturn("# title");

        DocumentUploadResponse response = documentService.uploadDocument(file);

        assertThat(response.name()).isEqualTo("note.md");
        assertThat(response.status()).isEqualTo("INDEXED");
    }

    @Test
    void marksUploadedDocumentAsFailedWhenParseRejectsContent() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", " ".getBytes());
        when(fileStorageService.store(file)).thenReturn(new StoredFile("empty.txt", "txt", "/tmp/empty.txt", 1L));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parserService.parse("/tmp/empty.txt"))
                .thenThrow(new IllegalArgumentException("Document content must not be empty"));

        DocumentUploadResponse response = documentService.uploadDocument(file);

        assertThat(response.name()).isEqualTo("empty.txt");
        assertThat(response.status()).isEqualTo("FAILED");
    }
}
