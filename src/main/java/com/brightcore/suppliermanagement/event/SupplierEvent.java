package com.brightcore.suppliermanagement.event;

import com.brightcore.suppliermanagement.dto.SupplierDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Wire-format for messages published to the supplier-events Kafka topic.
 * Includes a stable eventId so downstream consumers can deduplicate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplierEvent {

    private String eventId;
    private EventType eventType;
    private Long supplierId;
    private SupplierDto.Response payload;
    private Instant occurredAt;
    private String source;

    public static SupplierEvent of(EventType type, Long supplierId, SupplierDto.Response payload) {
        return SupplierEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(type)
                .supplierId(supplierId)
                .payload(payload)
                .occurredAt(Instant.now())
                .source("supplier-management")
                .build();
    }
}
