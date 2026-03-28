package com.vlz.laborexchange_alertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Job alert subscription — defines criteria for matching new vacancies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertSubscriptionDto {

    @Schema(description = "Subscription ID", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "User ID (set from X-User-Id header)", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    @Schema(description = "User email (used for sending alerts)", accessMode = Schema.AccessMode.READ_ONLY)
    private String userEmail;

    @Schema(description = "Keywords to match in vacancy title or description (comma-separated)", example = "Java, Spring Boot")
    @Size(max = 500, message = "Keywords too long (max 500 characters)")
    private String keywords;

    @Schema(description = "Location filter", example = "Moscow")
    @Size(max = 255, message = "Location too long")
    private String location;

    @Schema(description = "Minimum salary filter", example = "80000")
    @PositiveOrZero(message = "minSalary must be >= 0")
    private Double minSalary;

    @Schema(description = "Maximum salary filter", example = "200000")
    @PositiveOrZero(message = "maxSalary must be >= 0")
    private Double maxSalary;

    @Schema(description = "Employment type filter", example = "FULL_TIME",
            allowableValues = {"FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP"})
    private String employmentType;

    @Schema(description = "Work format filter", example = "REMOTE",
            allowableValues = {"REMOTE", "OFFICE", "HYBRID"})
    private String workFormat;

    @Schema(description = "Skill IDs to match (any vacancy containing ANY of these skills will match)", example = "[1, 5, 12]")
    private List<Long> skillIds;

    @Schema(description = "Whether this subscription is active", example = "true")
    private boolean active;

    @Schema(description = "Created at", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
