package com.vlz.laborexchange_alertservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vlz.laborexchange_alertservice.dto.VacancyIndexEvent;
import com.vlz.laborexchange_alertservice.service.AlertSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyPublishedListener {

    private final ObjectMapper objectMapper;
    private final AlertSubscriptionService subscriptionService;

    @KafkaListener(
            topics = "${spring.kafka.topics.indexing-vacancy}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(String message) {
        try {
            VacancyIndexEvent event = objectMapper.readValue(message, VacancyIndexEvent.class);
            if (event.isDeleted()) {
                log.debug("Ignoring deleted vacancy id={}", event.getId());
                return;
            }
            subscriptionService.matchAndNotify(event);
        } catch (Exception e) {
            log.error("Error processing vacancy event for alerts", e);
            // Non-critical: don't rethrow, don't block indexing
        }
    }
}
