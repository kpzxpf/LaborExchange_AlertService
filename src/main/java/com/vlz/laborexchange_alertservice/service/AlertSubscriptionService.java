package com.vlz.laborexchange_alertservice.service;

import com.vlz.laborexchange_alertservice.dto.AlertSubscriptionDto;
import com.vlz.laborexchange_alertservice.dto.JobAlertEvent;
import com.vlz.laborexchange_alertservice.dto.VacancyIndexEvent;
import com.vlz.laborexchange_alertservice.entity.AlertSubscription;
import com.vlz.laborexchange_alertservice.mapper.AlertSubscriptionMapper;
import com.vlz.laborexchange_alertservice.producer.JobAlertProducer;
import com.vlz.laborexchange_alertservice.repository.AlertSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertSubscriptionService {

    private final AlertSubscriptionRepository repository;
    private final AlertSubscriptionMapper mapper;
    private final JobAlertProducer alertProducer;

    @Transactional
    public AlertSubscriptionDto create(Long userId, String userEmail, AlertSubscriptionDto dto) {
        dto.setUserId(userId);
        dto.setUserEmail(userEmail);
        dto.setActive(true);

        AlertSubscription entity = mapper.toEntity(dto);
        entity.setUserId(userId);
        entity.setUserEmail(userEmail);
        entity.setActive(true);

        AlertSubscriptionDto result = mapper.toDto(repository.save(entity));

        log.info("Alert subscription created: id={} userId={}", result.getId(), userId);

        return result;
    }

    @Transactional(readOnly = true)
    public List<AlertSubscriptionDto> getMySubscriptions(Long userId) {
        return mapper.toDtoList(repository.findAllByUserId(userId));
    }

    @Transactional
    public AlertSubscriptionDto toggle(Long subscriptionId, Long userId) {
        AlertSubscription sub = findByIdAndOwner(subscriptionId, userId);

        sub.setActive(!sub.isActive());
        AlertSubscriptionDto result = mapper.toDto(repository.save(sub));

        log.info("Alert subscription id={} toggled to active={} for userId={}", subscriptionId, result.isActive(), userId);

        return result;
    }

    @Transactional
    public void delete(Long subscriptionId, Long userId) {
        AlertSubscription sub = findByIdAndOwner(subscriptionId, userId);

        repository.delete(sub);

        log.info("Alert subscription deleted: id={} userId={}", subscriptionId, userId);
    }

    /**
     * Matches vacancy against all active subscriptions and publishes job-alert events.
     * Called by VacancyPublishedListener on indexing-vacancy events.
     * Non-critical: errors per subscription are caught and logged, never rethrown.
     */
    @Transactional(readOnly = true)
    public void matchAndNotify(VacancyIndexEvent vacancy) {
        if (vacancy.isDeleted() || vacancy.getTitle() == null) return;

        List<AlertSubscription> active = repository.findAllByIsActiveTrue();
        log.debug("Matching vacancy '{}' against {} subscriptions", vacancy.getTitle(), active.size());

        for (AlertSubscription sub : active) {
            if (matches(sub, vacancy)) {
                JobAlertEvent alert = JobAlertEvent.builder()
                        .subscriptionId(sub.getId())
                        .userId(sub.getUserId())
                        .userEmail(sub.getUserEmail())
                        .vacancyId(vacancy.getId())
                        .vacancyTitle(vacancy.getTitle())
                        .companyName(vacancy.getCompanyName())
                        .location(vacancy.getLocation())
                        .salary(vacancy.getSalary())
                        .employmentType(vacancy.getEmploymentType())
                        .workFormat(vacancy.getWorkFormat())
                        .skills(vacancy.getSkills())
                        .publishedAt(vacancy.getCreatedAt())
                        .build();
                try {
                    alertProducer.send(alert);
                    log.info("Job alert sent: userId={} vacancyId={}", sub.getUserId(), vacancy.getId());
                } catch (Exception e) {
                    log.error("Failed to send job alert: subscriptionId={} vacancyId={} error={}",
                            sub.getId(), vacancy.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * All criteria are ANDed — subscription matches only if ALL specified filters are satisfied.
     * keywords: any keyword found in title or description (case-insensitive, comma-separated)
     * location: vacancy location contains subscription location substring
     * salary: vacancy salary within [minSalary, maxSalary] range
     * employmentType / workFormat: exact match if set
     * skillIds: optimistic match — skill ID→name resolution requires a SkillService call (TODO)
     */
    boolean matches(AlertSubscription sub, VacancyIndexEvent vacancy) {
        if (sub.getKeywords() != null && !sub.getKeywords().isBlank()) {
            String[] keywords = sub.getKeywords().split(",");
            String searchable = ((vacancy.getTitle() != null ? vacancy.getTitle() : "") + " " +
                    (vacancy.getDescription() != null ? vacancy.getDescription() : "")).toLowerCase();
            boolean anyKeyword = Arrays.stream(keywords)
                    .map(String::trim)
                    .filter(k -> !k.isBlank())
                    .anyMatch(k -> searchable.contains(k.toLowerCase()));
            if (!anyKeyword) return false;
        }

        if (sub.getLocation() != null && !sub.getLocation().isBlank()) {
            if (vacancy.getLocation() == null) return false;
            if (!vacancy.getLocation().toLowerCase().contains(sub.getLocation().toLowerCase())) return false;
        }

        if (sub.getMinSalary() != null && sub.getMinSalary() > 0) {
            if (vacancy.getSalary() == null || vacancy.getSalary() < sub.getMinSalary()) return false;
        }
        if (sub.getMaxSalary() != null && sub.getMaxSalary() > 0) {
            if (vacancy.getSalary() != null && vacancy.getSalary() > sub.getMaxSalary()) return false;
        }

        if (sub.getEmploymentType() != null && !sub.getEmploymentType().isBlank()) {
            if (!sub.getEmploymentType().equalsIgnoreCase(vacancy.getEmploymentType())) return false;
        }

        if (sub.getWorkFormat() != null && !sub.getWorkFormat().isBlank()) {
            if (!sub.getWorkFormat().equalsIgnoreCase(vacancy.getWorkFormat())) return false;
        }

        if (sub.getSkillIds() != null && !sub.getSkillIds().isBlank()) {
            Set<String> vacancySkills = vacancy.getSkills();
            if (vacancySkills == null || vacancySkills.isEmpty()) return false;
            // IDs stored as "1,5,12" cannot be compared to skill names from VacancyIndexEvent
            // without resolving via SkillService — currently optimistic (always matches if vacancy has skills)
        }

        return true;
    }

    private AlertSubscription findByIdAndOwner(Long id, Long userId) {
        AlertSubscription sub = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found: " + id));

        if (!sub.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Subscription not found: " + id);
        }

        return sub;
    }
}
