package com.vlz.laborexchange_alertservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

/** Published to Kafka job-alert topic. NotificationService listens to send emails. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAlertEvent {
    private Long subscriptionId;
    private Long userId;
    private String userEmail;
    private Long vacancyId;
    private String vacancyTitle;
    private String companyName;
    private String location;
    private Double salary;
    private String employmentType;
    private String workFormat;
    private Set<String> skills;
    private LocalDateTime publishedAt;
}
