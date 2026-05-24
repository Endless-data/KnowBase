package com.zhen.knowbase;

import com.zhen.knowbase.repository.ChunkRepository;
import com.zhen.knowbase.repository.ChatRecordRepository;
import com.zhen.knowbase.repository.CitationRepository;
import com.zhen.knowbase.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "knowbase.embedding.provider=local",
        "knowbase.vector-store.provider=memory",
        "DEEPSEEK_API_KEY=test-key"
})
class KnowBaseApplicationTests {

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private ChunkRepository chunkRepository;

    @MockBean
    private ChatRecordRepository chatRecordRepository;

    @MockBean
    private CitationRepository citationRepository;

    @Test
    void contextLoads() {
    }
}
