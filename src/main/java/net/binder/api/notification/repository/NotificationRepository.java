package net.binder.api.notification.repository;

import net.binder.api.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Long countByReceiverIdAndIsRead(Long id, boolean isRead);

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.isRead = true
            WHERE n.receiver.id = :memberId AND n.isRead = false
            """)
    Integer updateUnreadToRead(Long memberId);
}
