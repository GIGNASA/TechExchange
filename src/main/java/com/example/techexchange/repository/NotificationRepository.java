package com.example.techexchange.repository;

import com.example.techexchange.entity.Notification;
import com.example.techexchange.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
}
