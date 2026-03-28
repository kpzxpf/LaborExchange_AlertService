package com.vlz.laborexchange_alertservice.mapper;

import com.vlz.laborexchange_alertservice.dto.AlertSubscriptionDto;
import com.vlz.laborexchange_alertservice.entity.AlertSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AlertSubscriptionMapper {

    @Mapping(target = "skillIds", source = "skillIds", qualifiedByName = "stringToList")
    @Mapping(target = "active", source = "active")
    AlertSubscriptionDto toDto(AlertSubscription entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "skillIds", source = "skillIds", qualifiedByName = "listToString")
    @Mapping(target = "isActive", source = "active")
    AlertSubscription toEntity(AlertSubscriptionDto dto);

    List<AlertSubscriptionDto> toDtoList(List<AlertSubscription> entities);

    @Named("stringToList")
    default List<Long> stringToList(String skillIds) {
        if (skillIds == null || skillIds.isBlank()) return List.of();
        return Arrays.stream(skillIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Named("listToString")
    default String listToString(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) return null;
        return skillIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
