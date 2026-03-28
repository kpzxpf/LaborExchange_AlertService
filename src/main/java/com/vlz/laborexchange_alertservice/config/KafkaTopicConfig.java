package com.vlz.laborexchange_alertservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topics.job-alert}")
    private String jobAlertTopic;

    @Bean
    public NewTopic jobAlertTopic() {
        return TopicBuilder.name(jobAlertTopic).partitions(1).replicas(1).build();
    }
}
