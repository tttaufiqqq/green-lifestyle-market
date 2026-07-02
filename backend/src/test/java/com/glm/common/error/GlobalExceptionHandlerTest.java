package com.glm.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDomain_returnsEnvelopeWithCorrectCodeAndStatus() {
        DomainException ex = new DomainException(ErrorCode.E_NOTFOUND, "Item not found", 404);

        ResponseEntity<ErrorResponse> response = handler.handleDomain(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("E-NOTFOUND");
        assertThat(response.getBody().error().message()).isEqualTo("Item not found");
        assertThat(response.getBody().error().errorId()).isNull();
    }

    @Test
    void handleUnexpected_returnsEInternalWithErrorId() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("E-INTERNAL");
        assertThat(response.getBody().error().errorId()).isNotBlank();
    }

    @Test
    void handleDomain_409_returnsConflictStatus() {
        DomainException ex = new DomainException(ErrorCode.E_ORD_STATE, "Wrong state", 409);

        ResponseEntity<ErrorResponse> response = handler.handleDomain(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error().code()).isEqualTo("E-ORD-STATE");
    }
}
