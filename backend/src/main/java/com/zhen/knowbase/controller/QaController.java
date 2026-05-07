package com.zhen.knowbase.controller;

import com.zhen.knowbase.common.ApiResponse;
import com.zhen.knowbase.dto.AskRequest;
import com.zhen.knowbase.dto.AskResponse;
import com.zhen.knowbase.service.QaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qa")
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    @PostMapping("/ask")
    public ApiResponse<AskResponse> ask(@RequestBody AskRequest request) {
        if (request == null || request.question() == null || request.question().isBlank()) {
            throw new IllegalArgumentException("Question must not be empty");
        }
        return ApiResponse.success(qaService.ask(request.question()));
    }
}
