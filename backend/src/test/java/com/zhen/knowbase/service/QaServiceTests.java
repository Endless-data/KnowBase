package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.AskResponse;
import com.zhen.knowbase.dto.RetrievedChunk;
import com.zhen.knowbase.entity.ChatRecord;
import com.zhen.knowbase.entity.Citation;
import com.zhen.knowbase.repository.ChatRecordRepository;
import com.zhen.knowbase.repository.CitationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QaServiceTests {

    private final RetrievalService retrievalService = mock(RetrievalService.class);
    private final ChatRecordRepository chatRecordRepository = mock(ChatRecordRepository.class);
    private final CitationRepository citationRepository = mock(CitationRepository.class);
    private final QaService qaService = new QaService(
            retrievalService,
            chatRecordRepository,
            citationRepository
    );

    @BeforeEach
    void setUp() {
        when(chatRecordRepository.save(any(ChatRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void answersWithTemplateAndCitationsWhenChunksAreFound() {
        RetrievedChunk chunk = new RetrievedChunk(
                10L,
                1L,
                "README.md",
                0,
                "KnowBase 是个人知识库系统。",
                0.9
        );
        when(retrievalService.retrieve("KnowBase 是什么", 3)).thenReturn(List.of(chunk));

        AskResponse response = qaService.ask("KnowBase 是什么");

        assertThat(response.answer()).startsWith("根据知识库内容，相关信息如下：");
        assertThat(response.answer()).contains("KnowBase 是个人知识库系统。");
        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().get(0).chunkId()).isEqualTo(10L);
        assertThat(response.citations().get(0).documentName()).isEqualTo("README.md");

        ArgumentCaptor<ChatRecord> chatRecordCaptor = ArgumentCaptor.forClass(ChatRecord.class);
        verify(chatRecordRepository).save(chatRecordCaptor.capture());
        ChatRecord savedChatRecord = chatRecordCaptor.getValue();
        assertThat(savedChatRecord.getQuestion()).isEqualTo("KnowBase 是什么");
        assertThat(savedChatRecord.getAnswer()).isEqualTo(response.answer());
        assertThat(savedChatRecord.getRetrievalCount()).isEqualTo(1);

        ArgumentCaptor<List<Citation>> citationsCaptor = ArgumentCaptor.forClass(List.class);
        verify(citationRepository).saveAll(citationsCaptor.capture());
        List<Citation> savedCitations = citationsCaptor.getValue();
        assertThat(savedCitations).hasSize(1);
        assertThat(savedCitations.get(0).getChatRecord()).isSameAs(savedChatRecord);
        assertThat(savedCitations.get(0).getChunkId()).isEqualTo(10L);
        assertThat(savedCitations.get(0).getDocumentId()).isEqualTo(1L);
        assertThat(savedCitations.get(0).getDocumentName()).isEqualTo("README.md");
        assertThat(savedCitations.get(0).getChunkIndex()).isZero();
        assertThat(savedCitations.get(0).getContent()).isEqualTo("KnowBase 是个人知识库系统。");
    }

    @Test
    void answersNoRelatedContentWhenNoChunksAreFound() {
        when(retrievalService.retrieve("不存在的问题", 3)).thenReturn(List.of());

        AskResponse response = qaService.ask("不存在的问题");

        assertThat(response.answer()).isEqualTo("知识库中暂无相关内容");
        assertThat(response.citations()).isEmpty();

        ArgumentCaptor<ChatRecord> chatRecordCaptor = ArgumentCaptor.forClass(ChatRecord.class);
        verify(chatRecordRepository).save(chatRecordCaptor.capture());
        ChatRecord savedChatRecord = chatRecordCaptor.getValue();
        assertThat(savedChatRecord.getQuestion()).isEqualTo("不存在的问题");
        assertThat(savedChatRecord.getAnswer()).isEqualTo("知识库中暂无相关内容");
        assertThat(savedChatRecord.getRetrievalCount()).isZero();
        verify(citationRepository).saveAll(List.of());
    }

    @Test
    void rejectsBlankQuestion() {
        assertThatThrownBy(() -> qaService.ask(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question must not be empty");
        verify(chatRecordRepository, never()).save(any(ChatRecord.class));
        verify(citationRepository, never()).saveAll(anyList());
    }
}
