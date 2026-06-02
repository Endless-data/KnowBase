package com.zhen.knowbase.controller;

import com.zhen.knowbase.dto.DocumentResponse;
import com.zhen.knowbase.dto.DocumentUploadResponse;
import com.zhen.knowbase.service.DocumentService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.multipart.MultipartFile;

@WebMvcTest(DocumentController.class)
class DocumentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void listsDocumentsWithUnifiedResponse() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 26, 10, 30);
        when(documentService.listDocuments()).thenReturn(List.of(
                new DocumentResponse(1L, "README.md", "md", "INDEXED", createdAt)
        ));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("README.md"))
                .andExpect(jsonPath("$.data[0].fileType").value("md"))
                .andExpect(jsonPath("$.data[0].status").value("INDEXED"));
    }

    @Test
    void uploadsDocumentWithUnifiedResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "README.md",
                "text/markdown",
                "# KnowBase".getBytes()
        );
        when(documentService.uploadDocument(file)).thenReturn(
                new DocumentUploadResponse(1L, "README.md", "INDEXED")
        );

        mockMvc.perform(multipart("/api/documents/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("上传成功"))
                .andExpect(jsonPath("$.data.documentId").value(1))
                .andExpect(jsonPath("$.data.name").value("README.md"))
                .andExpect(jsonPath("$.data.status").value("INDEXED"));
    }

    @Test
    void deletesDocumentWithUnifiedResponse() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void batchUploadsDocumentsWithPartialSuccess() throws Exception {
        MockMultipartFile first = new MockMultipartFile(
                "files",
                "first.md",
                "text/markdown",
                "# First".getBytes()
        );
        MockMultipartFile second = new MockMultipartFile(
                "files",
                "bad.pdf",
                "application/pdf",
                "bad".getBytes()
        );
        when(documentService.uploadDocument(any(MultipartFile.class)))
                .thenReturn(new DocumentUploadResponse(1L, "first.md", "INDEXED"))
                .thenThrow(new IllegalArgumentException("Only .txt and .md files are allowed"));

        mockMvc.perform(multipart("/api/documents/batch-upload").file(first).file(second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量上传完成"))
                .andExpect(jsonPath("$.data.results[0].fileName").value("first.md"))
                .andExpect(jsonPath("$.data.results[0].success").value(true))
                .andExpect(jsonPath("$.data.results[0].documentId").value(1))
                .andExpect(jsonPath("$.data.results[0].status").value("INDEXED"))
                .andExpect(jsonPath("$.data.results[1].fileName").value("bad.pdf"))
                .andExpect(jsonPath("$.data.results[1].success").value(false))
                .andExpect(jsonPath("$.data.results[1].message").value("Only .txt and .md files are allowed"));
    }

    @Test
    void batchDeletesDocumentsWithPartialSuccess() throws Exception {
        doThrow(new IllegalArgumentException("Document not found")).when(documentService).deleteDocument(404L);

        mockMvc.perform(post("/api/documents/batch-delete")
                        .contentType(APPLICATION_JSON)
                        .content("{\"documentIds\":[1,404]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("批量删除完成"))
                .andExpect(jsonPath("$.data.results[0].documentId").value(1))
                .andExpect(jsonPath("$.data.results[0].success").value(true))
                .andExpect(jsonPath("$.data.results[1].documentId").value(404))
                .andExpect(jsonPath("$.data.results[1].success").value(false))
                .andExpect(jsonPath("$.data.results[1].message").value("Document not found"));
    }
}
