package net.binder.api.notification.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public Notification sendNotification(Member sender, Member receiver, Bin bin, NotificationType type,
                                         String additionalInfo) {

        Notification notification = new Notification(sender, receiver, bin, type, additionalInfo);

        return notificationRepository.save(notification);
    }

    public List<Notification> sendNotificationForUsers(Member sender, List<Member> receivers, Bin bin,
                                                       NotificationType type,
                                                       String additionalInfo) {

        List<Notification> notifications = getNotifications(sender, receivers, bin,
                type, additionalInfo);

        return notificationRepository.saveAll(notifications);
    }

    private List<Notification> getNotifications(Member sender, List<Member> receivers, Bin bin,
                                                NotificationType type, String additionalInfo) {
        List<Notification> notifications = new ArrayList<>();

        for (Member receiver : receivers) {
            Notification notification = new Notification(sender, receiver, bin, type, additionalInfo);
            notifications.add(notification);
        }
        return Collections.unmodifiableList(notifications);
    }
}
