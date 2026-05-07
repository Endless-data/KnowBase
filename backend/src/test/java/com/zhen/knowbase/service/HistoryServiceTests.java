package com.zhen.knowbase.service;

import com.zhen.knowbase.dto.HistoryDetailResponse;
import com.zhen.knowbase.dto.HistoryListResponse;
import com.zhen.knowbase.entity.ChatRecord;
import com.zhen.knowbase.entity.Citation;
import com.zhen.knowbase.repository.ChatRecordRepository;
import com.zhen.knowbase.repository.CitationRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoryServiceTests {

    private final ChatRecordRepository chatRecordRepository = mock(ChatRecordRepository.class);
    private final CitationRepository citationRepository = mock(CitationRepository.class);
    private final HistoryService historyService = new HistoryService(chatRecordRepository, citationRepository);

    @Test
    void listsHistoryInRepositoryOrder() {
        ChatRecord first = chatRecord(1L, "first question", "first answer", 1);
        ChatRecord second = chatRecord(2L, "second question", "second answer", 0);
        when(chatRecordRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(second, first));

        List<HistoryListResponse> responses = historyService.listHistory();

        assertThat(responses).extracting(HistoryListResponse::id).containsExactly(2L, 1L);
        assertThat(responses.get(0).question()).isEqualTo("second question");
        assertThat(responses.get(0).retrievalCount()).isZero();
    }

    @Test
    void getsHistoryDetailWithCitations() {
        ChatRecord chatRecord = chatRecord(1L, "KnowBase", "answer", 1);
        Citation citation = citation(chatRecord, 10L, 2L, "README.md", 0, "content");
        when(chatRecordRepository.findById(1L)).thenReturn(Optional.of(chatRecord));
        when(citationRepository.findByChatRecordIdOrderByChunkIndexAsc(1L)).thenReturn(List.of(citation));

        HistoryDetailResponse response = historyService.getHistory(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.question()).isEqualTo("KnowBase");
        assertThat(response.answer()).isEqualTo("answer");
        assertThat(response.retrievalCount()).isEqualTo(1);
        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().get(0).chunkId()).isEqualTo(10L);
        assertThat(response.citations().get(0).documentName()).isEqualTo("README.md");
    }

    @Test
    void rejectsMissingHistoryRecord() {
        when(chatRecordRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.getHistory(404L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("History record not found");
    }

    private ChatRecord chatRecord(Long id, String question, String answer, Integer retrievalCount) {
        ChatRecord chatRecord = new ChatRecord(question, answer, retrievalCount);
        setId(chatRecord, id);
        return chatRecord;
    }

    private Citation citation(
            ChatRecord chatRecord,
            Long chunkId,
            Long documentId,
            String documentName,
            Integer chunkIndex,
            String content
    ) {
        return new Citation(chatRecord, chunkId, documentId, documentName, chunkIndex, content);
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to set test entity id", exception);
        }
    }
}
