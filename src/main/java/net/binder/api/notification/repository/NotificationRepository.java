package net.binder.api.notification.repository;

import net.binder.api.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Long countByReceiverIdAndIsRead(Long id, boolean isRead);
}
