package com.vlz.laborexchange_alertservice.mapper;

import com.vlz.laborexchange_alertservice.dto.AlertSubscriptionDto;
import com.vlz.laborexchange_alertservice.entity.AlertSubscription;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-23T23:34:12+0500",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.0.0.jar, environment: Java 17.0.18 (Microsoft)"
)
@Component
public class AlertSubscriptionMapperImpl implements AlertSubscriptionMapper {

    @Override
    public AlertSubscriptionDto toDto(AlertSubscription entity) {
        if ( entity == null ) {
            return null;
        }

        AlertSubscriptionDto.AlertSubscriptionDtoBuilder alertSubscriptionDto = AlertSubscriptionDto.builder();

        alertSubscriptionDto.skillIds( stringToList( entity.getSkillIds() ) );
        alertSubscriptionDto.active( entity.isActive() );
        alertSubscriptionDto.id( entity.getId() );
        alertSubscriptionDto.userId( entity.getUserId() );
        alertSubscriptionDto.userEmail( entity.getUserEmail() );
        alertSubscriptionDto.keywords( entity.getKeywords() );
        alertSubscriptionDto.location( entity.getLocation() );
        alertSubscriptionDto.minSalary( entity.getMinSalary() );
        alertSubscriptionDto.maxSalary( entity.getMaxSalary() );
        alertSubscriptionDto.employmentType( entity.getEmploymentType() );
        alertSubscriptionDto.workFormat( entity.getWorkFormat() );
        alertSubscriptionDto.createdAt( entity.getCreatedAt() );

        return alertSubscriptionDto.build();
    }

    @Override
    public AlertSubscription toEntity(AlertSubscriptionDto dto) {
        if ( dto == null ) {
            return null;
        }

        AlertSubscription.AlertSubscriptionBuilder alertSubscription = AlertSubscription.builder();

        alertSubscription.skillIds( listToString( dto.getSkillIds() ) );
        alertSubscription.isActive( dto.isActive() );
        alertSubscription.userId( dto.getUserId() );
        alertSubscription.userEmail( dto.getUserEmail() );
        alertSubscription.keywords( dto.getKeywords() );
        alertSubscription.location( dto.getLocation() );
        alertSubscription.minSalary( dto.getMinSalary() );
        alertSubscription.maxSalary( dto.getMaxSalary() );
        alertSubscription.employmentType( dto.getEmploymentType() );
        alertSubscription.workFormat( dto.getWorkFormat() );

        return alertSubscription.build();
    }

    @Override
    public List<AlertSubscriptionDto> toDtoList(List<AlertSubscription> entities) {
        if ( entities == null ) {
            return null;
        }

        List<AlertSubscriptionDto> list = new ArrayList<AlertSubscriptionDto>( entities.size() );
        for ( AlertSubscription alertSubscription : entities ) {
            list.add( toDto( alertSubscription ) );
        }

        return list;
    }
}
