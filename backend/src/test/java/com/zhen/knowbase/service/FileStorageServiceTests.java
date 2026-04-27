package com.zhen.knowbase.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTests {

    @TempDir
    private Path tempDir;

    @Test
    void storesAllowedFile() {
        FileStorageService service = new FileStorageService(
                tempDir.toString(),
                1024,
                List.of("txt", "md")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.md",
                "text/markdown",
                "hello".getBytes()
        );

        FileStorageService.StoredFile storedFile = service.store(file);

        assertThat(storedFile.originalFilename()).isEqualTo("note.md");
        assertThat(storedFile.fileType()).isEqualTo("md");
        assertThat(storedFile.fileSize()).isEqualTo(5L);
        assertThat(Files.exists(Path.of(storedFile.filePath()))).isTrue();
    }

    @Test
    void rejectsUnsupportedFileType() {
        FileStorageService service = new FileStorageService(
                tempDir.toString(),
                1024,
                List.of("txt", "md")
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "data".getBytes()
        );

        assertThatThrownBy(() -> service.store(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported file type");
    }
}
