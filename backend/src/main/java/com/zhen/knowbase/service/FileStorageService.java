package com.zhen.knowbase.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private final long maxFileSize;
    private final List<String> allowedExtensions;

    public FileStorageService(
            @Value("${knowbase.storage.upload-dir}") String uploadDir,
            @Value("${knowbase.storage.max-file-size}") long maxFileSize,
            @Value("${knowbase.storage.allowed-extensions}") List<String> allowedExtensions
    ) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        this.allowedExtensions = allowedExtensions.stream()
                .map(extension -> extension.toLowerCase(Locale.ROOT))
                .toList();
    }

    public StoredFile store(MultipartFile file) {
        validate(file);

        String originalFilename = file.getOriginalFilename();
        String safeFilename = sanitizeFilename(originalFilename);
        String extension = extractExtension(safeFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path targetPath = uploadDir.resolve(storedFilename).normalize();

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(targetPath);
            return new StoredFile(originalFilename, extension, targetPath.toString(), file.getSize());
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to store uploaded file", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Uploaded file size exceeds limit");
        }

        String filename = sanitizeFilename(file.getOriginalFilename());
        String extension = extractExtension(filename);
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type");
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Uploaded file name must not be empty");
        }

        Path filenamePath = Path.of(filename).getFileName();
        if (filenamePath == null) {
            throw new IllegalArgumentException("Uploaded file name must not be empty");
        }

        return filenamePath.toString();
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new IllegalArgumentException("Uploaded file must have an extension");
        }

        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    public void deleteStoredFile(String filePath) {
        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to delete stored file", exception);
        }
    }

    public record StoredFile(
            String originalFilename,
            String fileType,
            String filePath,
            Long fileSize
    ) {
    }
}
