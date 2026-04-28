package com.zhen.knowbase.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class ParserService {

    public String parse(String filePath) {
        try {
            String content = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
            if (content.isBlank()) {
                throw new IllegalArgumentException("Document content must not be empty");
            }
            return content;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to parse document", exception);
        }
    }
}
