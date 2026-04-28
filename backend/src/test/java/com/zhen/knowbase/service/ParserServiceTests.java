package com.zhen.knowbase.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParserServiceTests {

    @TempDir
    private Path tempDir;

    private final ParserService parserService = new ParserService();

    @Test
    void parsesTextFileContent() throws IOException {
        Path documentPath = tempDir.resolve("note.md");
        Files.writeString(documentPath, "# KnowBase\nhello");

        String content = parserService.parse(documentPath.toString());

        assertThat(content).isEqualTo("# KnowBase\nhello");
    }

    @Test
    void rejectsBlankContent() throws IOException {
        Path documentPath = tempDir.resolve("empty.txt");
        Files.writeString(documentPath, "   \n\t");

        assertThatThrownBy(() -> parserService.parse(documentPath.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Document content must not be empty");
    }
}
