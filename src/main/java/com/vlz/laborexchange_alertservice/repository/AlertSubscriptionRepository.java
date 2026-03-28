package com.vlz.laborexchange_alertservice.repository;

import com.vlz.laborexchange_alertservice.entity.AlertSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlertSubscriptionRepository extends JpaRepository<AlertSubscription, Long> {
    List<AlertSubscription> findAllByUserId(Long userId);
    List<AlertSubscription> findAllByIsActiveTrue();

    @Query("SELECT s FROM AlertSubscription s WHERE s.isActive = true AND s.userId = :userId")
    List<AlertSubscription> findActiveByUserId(Long userId);
}
