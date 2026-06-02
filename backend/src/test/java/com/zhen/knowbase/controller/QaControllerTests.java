package com.zhen.knowbase.controller;

import com.zhen.knowbase.dto.AskResponse;
import com.zhen.knowbase.dto.CitationResponse;
import com.zhen.knowbase.service.QaService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QaController.class)
class QaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QaService qaService;

    @Test
    void asksQuestionWithUnifiedResponse() throws Exception {
        when(qaService.ask("KnowBase 是什么")).thenReturn(new AskResponse(
                "根据知识库内容，相关信息如下：\n\nKnowBase 是个人知识库系统。",
                List.of(new CitationResponse(10L, 1L, "README.md", 0, "KnowBase 是个人知识库系统。"))
        ));

        mockMvc.perform(post("/api/qa/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"KnowBase 是什么\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.answer").value("根据知识库内容，相关信息如下：\n\nKnowBase 是个人知识库系统。"))
                .andExpect(jsonPath("$.data.citations[0].chunkId").value(10))
                .andExpect(jsonPath("$.data.citations[0].documentId").value(1))
                .andExpect(jsonPath("$.data.citations[0].documentName").value("README.md"))
                .andExpect(jsonPath("$.data.citations[0].chunkIndex").value(0));
    }

    @Test
    void rejectsBlankQuestionWithUnifiedResponse() throws Exception {
        mockMvc.perform(post("/api/qa/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Question must not be empty"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void rejectsBlankStreamQuestionWithUnifiedResponse() throws Exception {
        mockMvc.perform(post("/api/qa/ask/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Question must not be empty"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}
