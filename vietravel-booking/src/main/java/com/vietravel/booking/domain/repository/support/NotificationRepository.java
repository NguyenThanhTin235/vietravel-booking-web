package com.vietravel.booking.domain.repository.support;

import com.vietravel.booking.domain.entity.support.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
     List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

     long countByUserIdAndReadFalse(Long userId);

     Optional<Notification> findByIdAndUserId(Long id, Long userId);

     void deleteByIdAndUserId(Long id, Long userId);
}
