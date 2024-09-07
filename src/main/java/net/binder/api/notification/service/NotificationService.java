package net.binder.api.notification.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.member.entity.Member;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification sendNotification(Member receiver, Member sender, Bin bin, NotificationType type,
                                         String additionalInfo) {

        Notification notification = new Notification(receiver, sender, bin, type, additionalInfo);

        return notificationRepository.save(notification);
    }
}
