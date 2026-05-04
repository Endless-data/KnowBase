package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.DocumentUploadResponse;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.entity.DocumentStatus;
import com.zhen.knowbase.repository.DocumentRepository;
import com.zhen.knowbase.service.FileStorageService.StoredFile;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    @Test
    void deletesDocumentChunksRecordAndStoredFile() {
        Document document = new Document("note.md", "md", "/tmp/note.md", DocumentStatus.INDEXED, 7L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        documentService.deleteDocument(1L);

        verify(chunkService).deleteChunksByDocumentId(1L);
        verify(documentRepository).delete(document);
        verify(fileStorageService).deleteStoredFile("/tmp/note.md");
    }

    @Test
    void rejectsDeletingMissingDocument() {
        when(documentRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.deleteDocument(404L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Document not found");
    }
}
