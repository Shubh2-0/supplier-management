package com.brightcore.suppliermanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Container for all Supplier-related Data Transfer Objects.
 *
 * Nested classes keep the API surface tightly coupled to a single concept
 * while still giving Jackson and Swagger clear, distinct schemas.
 */
public class SupplierDto {

    private SupplierDto() {
        // utility container
    }

    /** Payload accepted by POST /api/v1/suppliers/add */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SupplierCreateRequest", description = "Payload for creating a new supplier")
    public static class CreateRequest {

        @NotBlank
        @Size(min = 2, max = 150)
        @Schema(example = "Acme Textiles")
        private String name;

        @NotBlank
        @Email
        @Size(max = 150)
        @Schema(example = "contact@acme.example")
        private String email;

        @Pattern(regexp = "^[+]?[0-9 ()-]{7,30}$", message = "phoneNumber must be a valid phone format")
        @Schema(example = "+91 9876543210")
        private String phoneNumber;

        @Size(max = 150)
        @Schema(example = "Acme Pvt Ltd")
        private String companyName;

        @Size(max = 255)
        private String address;

        @Size(max = 100)
        @Schema(example = "India")
        private String country;
    }

    /** Payload accepted by PUT /api/v1/suppliers/update/{id} */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SupplierUpdateRequest", description = "Payload for updating an existing supplier")
    public static class UpdateRequest {

        @NotBlank
        @Size(min = 2, max = 150)
        private String name;

        @NotBlank
        @Email
        @Size(max = 150)
        private String email;

        @Pattern(regexp = "^[+]?[0-9 ()-]{7,30}$", message = "phoneNumber must be a valid phone format")
        private String phoneNumber;

        @Size(max = 150)
        private String companyName;

        @Size(max = 255)
        private String address;

        @Size(max = 100)
        private String country;

        private Boolean active;
    }

    /** Standard response shape for any Supplier returned by the API. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(name = "SupplierResponse", description = "Supplier resource representation")
    public static class Response {
        private Long id;
        private String name;
        private String email;
        private String phoneNumber;
        private String companyName;
        private String address;
        private String country;
        private Boolean active;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
