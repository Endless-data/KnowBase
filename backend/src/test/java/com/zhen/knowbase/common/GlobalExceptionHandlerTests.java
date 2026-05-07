package com.zhen.knowbase.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesIllegalArgumentExceptionWithUnifiedResponse() {
        ApiResponse<Void> response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Invalid request")
        );

        assertThat(response.code()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("Invalid request");
        assertThat(response.data()).isNull();
    }

    @Test
    void handlesExceptionWithUnifiedResponse() {
        ApiResponse<Void> response = handler.handleException(new RuntimeException("Unexpected error"));

        assertThat(response.code()).isEqualTo(500);
        assertThat(response.message()).isEqualTo("Internal server error");
        assertThat(response.data()).isNull();
    }

    @Test
    void handlesInvalidRequestBodyWithUnifiedResponse() {
        ApiResponse<Void> response = handler.handleHttpMessageNotReadableException(
                new HttpMessageNotReadableException("Invalid JSON")
        );

        assertThat(response.code()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("Request body is invalid");
        assertThat(response.data()).isNull();
    }

    @Test
    void handlesMissingUploadedFileWithUnifiedResponse() throws Exception {
        ApiResponse<Void> response = handler.handleMissingServletRequestPartException(
                new MissingServletRequestPartException("file")
        );

        assertThat(response.code()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("Uploaded file is required");
        assertThat(response.data()).isNull();
    }

    @Test
    void handlesOversizedUploadedFileWithUnifiedResponse() {
        ApiResponse<Void> response = handler.handleMaxUploadSizeExceededException(
                new MaxUploadSizeExceededException(1024)
        );

        assertThat(response.code()).isEqualTo(400);
        assertThat(response.message()).isEqualTo("Uploaded file size exceeds limit");
        assertThat(response.data()).isNull();
    }
}
