package com.example.online_pharmacy.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void successShouldCreateSuccessfulResultWithData() {
        Result<String> result = Result.success("ok");

        assertThat(result.getCode()).isEqualTo(Result.SUCCESS_CODE);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo("ok");
        assertThat(result.getTimestamp()).isPositive();
    }

    @Test
    void failureShouldCreateBusinessErrorResult() {
        Result<Void> result = Result.failure("bad request");

        assertThat(result.getCode()).isEqualTo(Result.BUSINESS_ERROR_CODE);
        assertThat(result.getMessage()).isEqualTo("bad request");
        assertThat(result.getData()).isNull();
        assertThat(result.getTimestamp()).isPositive();
    }

    @Test
    void failureShouldUseCustomErrorCode() {
        Result<Void> result = Result.failure(499, "custom");

        assertThat(result.getCode()).isEqualTo(499);
        assertThat(result.getMessage()).isEqualTo("custom");
    }
}
