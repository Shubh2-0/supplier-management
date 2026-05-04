package com.brightcore.suppliermanagement.kafka;

import com.brightcore.suppliermanagement.event.SupplierEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierKafkaProducer {

    private final KafkaTemplate<String, SupplierEvent> kafkaTemplate;

    @Value("${app.kafka.topic.supplier-events}")
    private String topic;

    public void publish(SupplierEvent event) {
        // Key by supplierId so all events for the same supplier land in the same partition
        // (preserves event ordering per supplier).
        String key = event.getSupplierId() != null ? event.getSupplierId().toString() : event.getEventId();

        CompletableFuture<SendResult<String, SupplierEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish SupplierEvent eventId={} type={} supplierId={}",
                        event.getEventId(), event.getEventType(), event.getSupplierId(), ex);
            } else {
                log.info("Published SupplierEvent eventId={} type={} supplierId={} partition={} offset={}",
                        event.getEventId(),
                        event.getEventType(),
                        event.getSupplierId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
