package com.expensora.expensora_api.repository;

import com.expensora.expensora_api.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndReadOrderByCreatedAtDesc(UUID userId, Boolean read);

}
