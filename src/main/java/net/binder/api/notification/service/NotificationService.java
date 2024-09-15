package net.binder.api.notification.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.notification.dto.NotificationDetail;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.repository.NotificationQueryRepository;
import net.binder.api.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private static final int PAGE_SIZE = 20;

    private final MemberService memberService;

    private final NotificationRepository notificationRepository;

    private final NotificationQueryRepository notificationQueryRepository;

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

    @Transactional(readOnly = true)
    public List<NotificationDetail> getNotificationDetails(String email, Long lastNotificationId) {
        Member member = memberService.findByEmail(email);

        List<Notification> notifications = notificationQueryRepository.findAllByMemberId(member.getId(),
                lastNotificationId, PAGE_SIZE);

        return notifications.stream()
                .map(NotificationDetail::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(String email) {
        Member member = memberService.findByEmail(email);

        return notificationRepository.countByReceiverIdAndIsRead(member.getId(), false);
    }

    public Integer readAllNotifications(String email) {
        Member member = memberService.findByEmail(email);
        return notificationRepository.updateUnreadToRead(member.getId());
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
