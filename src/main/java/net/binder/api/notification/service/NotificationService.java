package net.binder.api.notification.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
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

    // 단순 로그 활용 목적 저장
    public Notification saveNotificationWithNoReceiver(Member sender, Bin bin, NotificationType type,
                                                       String additionalInfo) {

        Notification notification = new Notification(sender, null, bin, type, additionalInfo);

        return notificationRepository.save(notification);
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

    @Transactional(readOnly = true)
    public boolean hasUnreadNotifications(String email) {
        Member member = memberService.findByEmail(email);
        return notificationRepository.existsByReceiverIdAndIsRead(member.getId(), false);
    }

    public void deleteNotification(String email, Long notificationId) {
        Member member = memberService.findByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 알림입니다."));

        validateIsOwner(notification, member);

        boolean isDeleted = notification.softDelete();
        validateIsAlreadyDeleted(isDeleted);
    }

    @Transactional(readOnly = true)
    public boolean hasLikeNotification(Member sender, Bin bin) {
        return notificationRepository.existsLike(sender.getId(), bin.getId());
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

    private void validateIsOwner(Notification notification, Member member) {
        if (!notification.isOwner(member)) {
            throw new BadRequestException("해당 알림 삭제 권한이 없는 사용자입니다.");
        }
    }

    private void validateIsAlreadyDeleted(boolean isDeleted) {
        if (!isDeleted) {
            throw new BadRequestException("이미 삭제된 알림입니다.");
        }
    }
}
