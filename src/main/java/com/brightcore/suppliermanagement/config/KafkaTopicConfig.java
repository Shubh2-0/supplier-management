package com.brightcore.suppliermanagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topic.supplier-events}")
    private String supplierEventsTopic;

    @Value("${app.kafka.topic.partitions:3}")
    private int partitions;

    @Value("${app.kafka.topic.replicas:1}")
    private short replicas;

    /**
     * Topic is auto-created on broker start. Declared here so the topology is
     * version-controlled rather than relying on first-publish auto-create.
     */
    @Bean
    public NewTopic supplierEventsTopic() {
        return TopicBuilder.name(supplierEventsTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
