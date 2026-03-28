package com.vlz.laborexchange_alertservice.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vlz.laborexchange_alertservice.dto.JobAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobAlertProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.job-alert}")
    private String topic;

    public void send(JobAlertEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, String.valueOf(event.getUserId()), message);
            log.debug("Sent job alert for userId={} vacancyId={}", event.getUserId(), event.getVacancyId());
        } catch (Exception e) {
            log.error("Failed to serialize job alert event", e);
            throw new RuntimeException("Failed to send job alert", e);
        }
    }
}
