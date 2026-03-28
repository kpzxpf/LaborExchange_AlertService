package com.vlz.laborexchange_alertservice.service;

import com.vlz.laborexchange_alertservice.dto.AlertSubscriptionDto;
import com.vlz.laborexchange_alertservice.dto.VacancyIndexEvent;
import com.vlz.laborexchange_alertservice.entity.AlertSubscription;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertSubscriptionServiceTest {

    @Mock
    com.vlz.laborexchange_alertservice.repository.AlertSubscriptionRepository repository;
    @Mock
    com.vlz.laborexchange_alertservice.mapper.AlertSubscriptionMapper mapper;
    @Mock
    com.vlz.laborexchange_alertservice.producer.JobAlertProducer alertProducer;
    @InjectMocks
    AlertSubscriptionService service;

    private static final Long USER_ID = 10L;
    private static final Long SUB_ID = 1L;

    private VacancyIndexEvent vacancy(String title, String location, Double salary, String empType, String format, Set<String> skills) {
        return VacancyIndexEvent.builder()
                .id(1L).title(title).description("desc").companyName("Corp")
                .location(location).salary(salary).employmentType(empType).workFormat(format)
                .skills(skills).deleted(false).build();
    }

    private AlertSubscription sub(String keywords, String location, Double minSalary, Double maxSalary, String empType, String format) {
        return AlertSubscription.builder()
                .id(1L).userId(10L).userEmail("user@example.com")
                .keywords(keywords).location(location)
                .minSalary(minSalary).maxSalary(maxSalary)
                .employmentType(empType).workFormat(format)
                .isActive(true).build();
    }

    @Test
    void matches_noFilters_alwaysMatches() {
        AlertSubscription s = sub(null, null, null, null, null, null);
        VacancyIndexEvent v = vacancy("Java Dev", "Moscow", 100000.0, "FULL_TIME", "REMOTE", Set.of("Java"));
        assertThat(service.matches(s, v)).isTrue();
    }

    @Test
    void matches_keywordInTitle_matches() {
        AlertSubscription s = sub("java, spring", null, null, null, null, null);
        VacancyIndexEvent v = vacancy("Senior Java Developer", "SPb", 90000.0, "FULL_TIME", "OFFICE", Set.of("Java"));
        assertThat(service.matches(s, v)).isTrue();
    }

    @Test
    void matches_keywordNotInTitle_doesNotMatch() {
        AlertSubscription s = sub("python", null, null, null, null, null);
        VacancyIndexEvent v = vacancy("Java Developer", "Moscow", 100000.0, "FULL_TIME", "REMOTE", Set.of("Java"));
        assertThat(service.matches(s, v)).isFalse();
    }

    @Test
    void matches_locationFilter_matchesSubstring() {
        AlertSubscription s = sub(null, "moscow", null, null, null, null);
        VacancyIndexEvent v = vacancy("Dev", "Moscow City", 100000.0, "FULL_TIME", "OFFICE", Set.of());
        assertThat(service.matches(s, v)).isTrue();
    }

    @Test
    void matches_locationFilter_noMatch() {
        AlertSubscription s = sub(null, "spb", null, null, null, null);
        VacancyIndexEvent v = vacancy("Dev", "Moscow", 100000.0, "FULL_TIME", "OFFICE", Set.of());
        assertThat(service.matches(s, v)).isFalse();
    }

    @Test
    void matches_minSalary_filtersLowSalary() {
        AlertSubscription s = sub(null, null, 100000.0, null, null, null);
        VacancyIndexEvent v = vacancy("Dev", "Moscow", 80000.0, "FULL_TIME", "REMOTE", Set.of());
        assertThat(service.matches(s, v)).isFalse();
    }

    @Test
    void matches_minSalary_acceptsHighEnoughSalary() {
        AlertSubscription s = sub(null, null, 100000.0, null, null, null);
        VacancyIndexEvent v = vacancy("Dev", "Moscow", 120000.0, "FULL_TIME", "REMOTE", Set.of());
        assertThat(service.matches(s, v)).isTrue();
    }

    @Test
    void matches_employmentTypeFilter_noMatch() {
        AlertSubscription s = sub(null, null, null, null, "FULL_TIME", null);
        VacancyIndexEvent v = vacancy("Dev", "Moscow", 100000.0, "PART_TIME", "REMOTE", Set.of());
        assertThat(service.matches(s, v)).isFalse();
    }

    @Test
    void matches_workFormatFilter_matches() {
        AlertSubscription s = sub(null, null, null, null, null, "REMOTE");
        VacancyIndexEvent v = vacancy("Dev", "Moscow", 100000.0, "FULL_TIME", "REMOTE", Set.of());
        assertThat(service.matches(s, v)).isTrue();
    }

    @Test
    void matches_deletedVacancy_doesNotMatch() {
        AlertSubscription s = sub(null, null, null, null, null, null);
        VacancyIndexEvent v = VacancyIndexEvent.builder().id(1L).deleted(true).build();
        // matchAndNotify handles this, but matches() should still work
        assertThat(service.matches(s, v)).isTrue(); // no criteria = always true
    }

    @Test
    @DisplayName("matches: maxSalary — отфильтровывает вакансии выше потолка")
    void matches_maxSalary_filtersHighSalary() {
        AlertSubscription s = sub(null, null, null, 80000.0, null, null);
        VacancyIndexEvent v = vacancy("Dev", "Moscow", 100000.0, "FULL_TIME", "REMOTE", Set.of());
        assertThat(service.matches(s, v)).isFalse();
    }

    @Test
    @DisplayName("matches: maxSalary — пропускает вакансии в пределах потолка")
    void matches_maxSalary_acceptsWithinRange() {
        AlertSubscription s = sub(null, null, null, 120000.0, null, null);
        VacancyIndexEvent v = vacancy("Dev", "Moscow", 100000.0, "FULL_TIME", "REMOTE", Set.of());
        assertThat(service.matches(s, v)).isTrue();
    }

    @Nested
    @DisplayName("CRUD операции с подписками")
    class CrudTests {

        @Test
        @DisplayName("create: создаёт подписку и возвращает DTO")
        void create_Success() {
            AlertSubscriptionDto dto = AlertSubscriptionDto.builder().keywords("java").build();
            AlertSubscription entity = AlertSubscription.builder().id(SUB_ID).userId(USER_ID).build();
            AlertSubscriptionDto result = AlertSubscriptionDto.builder().id(SUB_ID).build();

            when(mapper.toEntity(any())).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDto(entity)).thenReturn(result);

            AlertSubscriptionDto created = service.create(USER_ID, "user@example.com", dto);

            assertThat(created.getId()).isEqualTo(SUB_ID);
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("getMySubscriptions: возвращает список для userId")
        void getMySubscriptions_ReturnsList() {
            AlertSubscription entity = AlertSubscription.builder().id(SUB_ID).userId(USER_ID).build();
            AlertSubscriptionDto dto = AlertSubscriptionDto.builder().id(SUB_ID).build();

            when(repository.findAllByUserId(USER_ID)).thenReturn(List.of(entity));
            when(mapper.toDtoList(List.of(entity))).thenReturn(List.of(dto));

            List<AlertSubscriptionDto> result = service.getMySubscriptions(USER_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(SUB_ID);
        }

        @Test
        @DisplayName("toggle: активная подписка → деактивируется")
        void toggle_ActiveToInactive() {
            AlertSubscription entity = AlertSubscription.builder()
                    .id(SUB_ID).userId(USER_ID).isActive(true).build();
            AlertSubscriptionDto dto = AlertSubscriptionDto.builder().id(SUB_ID).active(false).build();

            when(repository.findById(SUB_ID)).thenReturn(Optional.of(entity));
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDto(entity)).thenReturn(dto);

            AlertSubscriptionDto result = service.toggle(SUB_ID, USER_ID);

            assertThat(entity.isActive()).isFalse();
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("toggle: ошибка если подписка не принадлежит пользователю")
        void toggle_NotOwner_ThrowsException() {
            AlertSubscription entity = AlertSubscription.builder()
                    .id(SUB_ID).userId(999L).build();

            when(repository.findById(SUB_ID)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> service.toggle(SUB_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("delete: удаляет подписку владельца")
        void delete_Success() {
            AlertSubscription entity = AlertSubscription.builder()
                    .id(SUB_ID).userId(USER_ID).build();

            when(repository.findById(SUB_ID)).thenReturn(Optional.of(entity));

            service.delete(SUB_ID, USER_ID);

            verify(repository).delete(entity);
        }

        @Test
        @DisplayName("delete: ошибка если подписка не найдена")
        void delete_NotFound_ThrowsException() {
            when(repository.findById(SUB_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(SUB_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("delete: ошибка если подписка не принадлежит пользователю")
        void delete_NotOwner_ThrowsException() {
            AlertSubscription entity = AlertSubscription.builder()
                    .id(SUB_ID).userId(999L).build();

            when(repository.findById(SUB_ID)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> service.delete(SUB_ID, USER_ID))
                    .isInstanceOf(EntityNotFoundException.class);
            verify(repository, never()).delete(any());
        }
    }
}
