package com.zhen.knowbase.common;

import org.junit.jupiter.api.Test;

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
}
