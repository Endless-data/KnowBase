package com.zhen.knowbase.controller;

import com.zhen.knowbase.dto.CitationResponse;
import com.zhen.knowbase.dto.HistoryDetailResponse;
import com.zhen.knowbase.dto.HistoryListResponse;
import com.zhen.knowbase.service.HistoryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
class HistoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoryService historyService;

    @Test
    void listsHistoryWithUnifiedResponse() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 7, 21, 30);
        when(historyService.listHistory()).thenReturn(List.of(
                new HistoryListResponse(1L, "KnowBase 是什么", "answer", 1, createdAt)
        ));

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].question").value("KnowBase 是什么"))
                .andExpect(jsonPath("$.data[0].answer").value("answer"))
                .andExpect(jsonPath("$.data[0].retrievalCount").value(1));
    }

    @Test
    void getsHistoryDetailWithUnifiedResponse() throws Exception {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 7, 21, 30);
        when(historyService.getHistory(1L)).thenReturn(new HistoryDetailResponse(
                1L,
                "KnowBase 是什么",
                "answer",
                1,
                createdAt,
                List.of(new CitationResponse(10L, 2L, "README.md", 0, "content"))
        ));

        mockMvc.perform(get("/api/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.question").value("KnowBase 是什么"))
                .andExpect(jsonPath("$.data.answer").value("answer"))
                .andExpect(jsonPath("$.data.citations[0].chunkId").value(10))
                .andExpect(jsonPath("$.data.citations[0].documentId").value(2))
                .andExpect(jsonPath("$.data.citations[0].documentName").value("README.md"));
    }

    @Test
    void deletesHistoryWithUnifiedResponse() throws Exception {
        mockMvc.perform(delete("/api/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("删除成功"));

        verify(historyService).deleteHistory(1L);
    }
}
