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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                new DocumentUploadResponse(1L, "README.md", "PARSING")
        );

        mockMvc.perform(multipart("/api/documents/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("上传成功"))
                .andExpect(jsonPath("$.data.documentId").value(1))
                .andExpect(jsonPath("$.data.name").value("README.md"))
                .andExpect(jsonPath("$.data.status").value("PARSING"));
    }
}
