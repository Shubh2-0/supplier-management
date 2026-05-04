package com.brightcore.suppliermanagement.kafka;

import com.brightcore.suppliermanagement.event.SupplierEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reference consumer that subscribes to {@code supplier-events} and writes
 * each event to the application log. In a real system this would back an
 * audit table, search index, or downstream notification service — having
 * a working consumer in-repo proves the publish/subscribe round-trip
 * actually works end-to-end.
 */
@Slf4j
@Component
public class SupplierAuditConsumer {

    @KafkaListener(
            topics = "${app.kafka.topic.supplier-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onSupplierEvent(
            @Payload SupplierEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("[AUDIT] partition={} offset={} type={} supplierId={} eventId={}",
                partition, offset, event.getEventType(), event.getSupplierId(), event.getEventId());
    }
}
