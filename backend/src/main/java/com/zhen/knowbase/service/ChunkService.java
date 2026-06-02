package com.zhen.knowbase.service;

import com.zhen.knowbase.entity.Chunk;
import com.zhen.knowbase.entity.Document;
import com.zhen.knowbase.repository.ChunkRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChunkService {

    static final int DEFAULT_CHUNK_SIZE = 800;
    static final int DEFAULT_CHUNK_OVERLAP = 120;
    private static final String SENTENCE_BOUNDARIES = "。！？.!?";

    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final int chunkSize;
    private final int chunkOverlap;

    public ChunkService(
            ChunkRepository chunkRepository,
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService,
            @Value("${knowbase.chunk.size:" + DEFAULT_CHUNK_SIZE + "}") int chunkSize,
            @Value("${knowbase.chunk.overlap:" + DEFAULT_CHUNK_OVERLAP + "}") int chunkOverlap
    ) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than 0");
        }
        if (chunkOverlap < 0 || chunkOverlap >= chunkSize) {
            throw new IllegalArgumentException("Chunk overlap must be greater than or equal to 0 and smaller than chunk size");
        }
        this.chunkRepository = chunkRepository;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public List<Chunk> createChunks(Document document, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Document content must not be empty");
        }

        List<Chunk> savedChunks = chunkRepository.saveAll(split(document, content));
        for (Chunk chunk : savedChunks) {
            List<Float> vector = embeddingService.embed(chunk.getContent());
            String vectorId = vectorStoreService.saveVector(chunk.getId(), vector);
            chunk.setVectorId(vectorId);
        }
        return chunkRepository.saveAll(savedChunks);
    }

    public void deleteChunksByDocumentId(Long documentId) {
        List<Long> chunkIds = chunkRepository.findByDocumentId(documentId)
                .stream()
                .map(Chunk::getId)
                .toList();
        vectorStoreService.deleteVectorsByChunkIds(chunkIds);
        chunkRepository.deleteByDocumentId(documentId);
    }

    private List<Chunk> split(Document document, String content) {
        List<Chunk> chunks = new ArrayList<>();
        List<String> chunkContents = buildChunkContents(normalize(content));
        int chunkIndex = 0;
        for (String chunkContent : chunkContents) {
            chunks.add(new Chunk(document, chunkIndex, chunkContent, null));
            chunkIndex++;
        }
        return chunks;
    }

    private List<String> buildChunkContents(String content) {
        List<String> units = splitIntoUnits(content);
        List<String> chunkContents = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String unit : units) {
            if (current.isEmpty()) {
                current.append(unit);
                continue;
            }

            String candidate = current + separatorBetween(current, unit) + unit;
            if (candidate.length() <= chunkSize) {
                current = new StringBuilder(candidate);
                continue;
            }

            String finishedChunk = current.toString().trim();
            chunkContents.add(finishedChunk);
            current = new StringBuilder(nextChunkSeed(finishedChunk, unit));
        }

        if (!current.isEmpty()) {
            chunkContents.add(current.toString().trim());
        }
        return chunkContents;
    }

    private String nextChunkSeed(String previousChunk, String unit) {
        String overlap = trailingOverlap(previousChunk);
        if (overlap.isEmpty()) {
            return unit;
        }

        String candidate = overlap + separatorBetween(overlap, unit) + unit;
        if (candidate.length() <= chunkSize) {
            return candidate;
        }
        return unit;
    }

    private List<String> splitIntoUnits(String content) {
        List<String> units = new ArrayList<>();
        for (String paragraph : content.split("\\n\\s*\\n")) {
            String normalizedParagraph = paragraph.trim();
            if (normalizedParagraph.isEmpty()) {
                continue;
            }
            appendParagraphUnits(units, normalizedParagraph);
        }
        return units;
    }

    private void appendParagraphUnits(List<String> units, String paragraph) {
        StringBuilder sentence = new StringBuilder();
        for (int index = 0; index < paragraph.length(); index++) {
            char current = paragraph.charAt(index);
            sentence.append(current);
            if (SENTENCE_BOUNDARIES.indexOf(current) >= 0) {
                appendUnit(units, sentence.toString().trim());
                sentence.setLength(0);
            }
        }

        appendUnit(units, sentence.toString().trim());
    }

    private void appendUnit(List<String> units, String unit) {
        if (unit.isEmpty()) {
            return;
        }
        if (unit.length() <= chunkSize) {
            units.add(unit);
            return;
        }

        for (int start = 0; start < unit.length(); start += chunkSize) {
            int end = Math.min(start + chunkSize, unit.length());
            units.add(unit.substring(start, end));
        }
    }

    private String trailingOverlap(String text) {
        if (chunkOverlap == 0 || text.length() <= chunkOverlap) {
            return "";
        }
        return text.substring(text.length() - chunkOverlap).trim();
    }

    private String separatorBetween(CharSequence left, String right) {
        if (left.isEmpty() || right.isEmpty()) {
            return "";
        }
        return "\n\n";
    }

    private String normalize(String content) {
        return content
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
