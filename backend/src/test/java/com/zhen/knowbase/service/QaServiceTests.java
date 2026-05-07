package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.AskResponse;
import com.zhen.knowbase.dto.RetrievedChunk;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QaServiceTests {

    private final RetrievalService retrievalService = mock(RetrievalService.class);
    private final QaService qaService = new QaService(retrievalService);

    @Test
    void answersWithTemplateAndCitationsWhenChunksAreFound() {
        RetrievedChunk chunk = new RetrievedChunk(
                10L,
                1L,
                "README.md",
                0,
                "KnowBase 是个人知识库系统。",
                2
        );
        when(retrievalService.retrieve("KnowBase 是什么", 3)).thenReturn(List.of(chunk));

        AskResponse response = qaService.ask("KnowBase 是什么");

        assertThat(response.answer()).startsWith("根据知识库内容，相关信息如下：");
        assertThat(response.answer()).contains("KnowBase 是个人知识库系统。");
        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().get(0).chunkId()).isEqualTo(10L);
        assertThat(response.citations().get(0).documentName()).isEqualTo("README.md");
    }

    @Test
    void answersNoRelatedContentWhenNoChunksAreFound() {
        when(retrievalService.retrieve("不存在的问题", 3)).thenReturn(List.of());

        AskResponse response = qaService.ask("不存在的问题");

        assertThat(response.answer()).isEqualTo("知识库中暂无相关内容");
        assertThat(response.citations()).isEmpty();
    }

    @Test
    void rejectsBlankQuestion() {
        assertThatThrownBy(() -> qaService.ask(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question must not be empty");
    }
}
