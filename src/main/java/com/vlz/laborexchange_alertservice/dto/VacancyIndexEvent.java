package com.vlz.laborexchange_alertservice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyIndexEvent {
    private long id;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private Set<String> skills;
    private Double salary;
    private String employmentType;
    private String workFormat;
    private LocalDateTime createdAt;
    private boolean deleted;
}
