package com.zhen.knowbase;

import com.zhen.knowbase.repository.DocumentRepository;
import com.zhen.knowbase.repository.ChunkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class KnowBaseApplicationTests {

    @MockBean
    private DocumentRepository documentRepository;

    @MockBean
    private ChunkRepository chunkRepository;

    @Test
    void contextLoads() {
    }
}
