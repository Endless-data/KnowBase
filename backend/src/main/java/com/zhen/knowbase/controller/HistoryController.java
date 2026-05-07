package com.zhen.knowbase.controller;

import com.zhen.knowbase.common.ApiResponse;
import com.zhen.knowbase.dto.HistoryDetailResponse;
import com.zhen.knowbase.dto.HistoryListResponse;
import com.zhen.knowbase.service.HistoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ApiResponse<List<HistoryListResponse>> listHistory() {
        return ApiResponse.success(historyService.listHistory());
    }

    @GetMapping("/{id}")
    public ApiResponse<HistoryDetailResponse> getHistory(@PathVariable Long id) {
        return ApiResponse.success(historyService.getHistory(id));
    }
}
