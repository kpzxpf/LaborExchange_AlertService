package com.vlz.laborexchange_alertservice.controller;

import com.vlz.laborexchange_alertservice.dto.AlertSubscriptionDto;
import com.vlz.laborexchange_alertservice.service.AlertSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Job Alert Subscriptions",
        description = "Subscribe to job alerts — receive email notifications when new matching vacancies are published")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertSubscriptionController {

    private final AlertSubscriptionService service;

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my alert subscriptions",
            description = "Returns all subscriptions for the authenticated user (active and inactive).")
    @ApiResponse(responseCode = "200", description = "List of subscriptions",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlertSubscriptionDto.class))))
    @GetMapping
    public List<AlertSubscriptionDto> getMy(
            @Parameter(description = "Authenticated user ID (from Gateway)") @RequestHeader("X-User-Id") Long userId) {
        return service.getMySubscriptions(userId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a job alert subscription",
            description = "Subscribe to matching vacancies. When a new vacancy is published that matches your criteria, you'll receive an email notification. " +
                    "All criteria are optional — leaving them empty means 'match anything'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription created",
                    content = @Content(schema = @Schema(implementation = AlertSubscriptionDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    public AlertSubscriptionDto create(
            @RequestBody @Valid AlertSubscriptionDto dto,
            @Parameter(description = "Authenticated user ID (from Gateway)") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Authenticated user email (from Gateway)") @RequestHeader("X-User-Email") String userEmail) {
        return service.create(userId, userEmail, dto);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Toggle subscription active/inactive",
            description = "Pauses or resumes a subscription without deleting it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription toggled",
                    content = @Content(schema = @Schema(implementation = AlertSubscriptionDto.class))),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @PatchMapping("/{id}/toggle")
    public AlertSubscriptionDto toggle(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        return service.toggle(id, userId);
    }

    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete a subscription")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found or not owned by user")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        service.delete(id, userId);
    }
}
