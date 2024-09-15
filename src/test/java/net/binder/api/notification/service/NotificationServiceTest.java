package net.binder.api.notification.service;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.notification.dto.NotificationDetail;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    NotificationService notificationService;

    @Autowired
    BinRepository binRepository;

    @Autowired
    MemberRepository memberRepository;

    private Member sender;

    private Member receiver;

    private Bin bin;

    @BeforeEach
    void setUp() {
        sender = new Member("sender@email.com", "sender", Role.ROLE_ADMIN, null);
        receiver = new Member("receiver@email.com", "receiver", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(sender, receiver));

        bin = new Bin("title", BinType.CIGAR, PointUtil.getPoint(100d, 10d), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
    }

    @Test
    @DisplayName("receiver의 읽지 않은 알림 개수를 확인할 수 있다.")
    void getUnreadCount() {
        //given
        Notification notification1 = new Notification(sender, receiver, bin, NotificationType.BIN_COMPLAINT_REJECTED,
                "info");
        Notification notification2 = new Notification(sender, receiver, bin, NotificationType.BIN_COMPLAINT_APPROVED,
                "info");
        Notification notification3 = new Notification(sender, receiver, bin, NotificationType.BIN_MODIFICATION_APPROVED,
                "info");

        notificationRepository.saveAll(List.of(notification1, notification2, notification3));
        notification3.markAsRead();

        //when
        Long unreadCount = notificationService.getUnreadCount(receiver.getEmail());

        //then
        assertThat(unreadCount).isEqualTo(2);
    }

    @Test
    @DisplayName("한명에게 알림 한건을 전송할 수 있다.")
    void sendNotification() {
        //when
        Notification notification = notificationService.sendNotification(sender, receiver, bin,
                NotificationType.BIN_COMPLAINT_REJECTED, "메시지");

        //then
        List<Notification> notifications = notificationRepository.findAll();

        assertThat(notifications.size()).isEqualTo(1);
        assertThat(notifications.get(0)).isEqualTo(notification);
        assertThat(notifications).extracting(Notification::getReceiver).containsExactly(receiver);
    }

    @Test
    @DisplayName("다수에게 같은 알림를 보낼 수 있다.")
    void sendNotificationForUsers() {
        Member receiver2 = new Member("receiver2@email.com", "receiver2", Role.ROLE_USER, null);
        Member receiver3 = new Member("receiver3@email.com", "receiver3", Role.ROLE_USER, null);

        //when
        List<Notification> notifications = notificationService.sendNotificationForUsers(sender,
                List.of(receiver, receiver2, receiver3), bin,
                NotificationType.BIN_COMPLAINT_REJECTED, null);

        assertThat(notifications).extracting(Notification::getReceiver).containsExactly(receiver, receiver2, receiver3);
    }

    @Test
    @DisplayName("마지막으로 읽은 알림을 기준으로 알림을 20개씩 확인할 수 있다.")
    void getNotificationDetails() {
        //given
        List<Notification> notifications = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Notification notification = new Notification(sender, receiver, bin, NotificationType.BIN_COMPLAINT_REJECTED,
                    "info");
            if (i % 10 == 0) {
                notification.markAsRead();
            }
            notifications.add(notification);
        }
        notificationRepository.saveAll(notifications);

        //when
        List<NotificationDetail> details1 = notificationService.getNotificationDetails(receiver.getEmail(),
                null);// null 일 경우 가장 최신부터 20개

        long lastId = details1.stream()
                .map(NotificationDetail::getNotificationId)
                .mapToLong(i -> i)
                .min().getAsLong(); // 마지막 id 찾기

        List<NotificationDetail> details2 = notificationService.getNotificationDetails(receiver.getEmail(),
                lastId); // 가장 오래된 id부터 10개

        //then
        assertThat(details1.size()).isEqualTo(20);
        assertThat(details2.size()).isEqualTo(10);
    }

    @Test
    @DisplayName("특정 멤버의 읽지 않은 알림을 모두 읽음처리 할 수 있다.")
    void readAllNotifications() {
        //given
        Notification notification1 = new Notification(sender, receiver, bin, NotificationType.BIN_COMPLAINT_REJECTED,
                "info");
        Notification notification2 = new Notification(sender, receiver, bin, NotificationType.BIN_COMPLAINT_APPROVED,
                "info");
        Notification notification3 = new Notification(sender, receiver, bin, NotificationType.BIN_MODIFICATION_APPROVED,
                "info");

        notification3.markAsRead();
        notificationRepository.saveAll(List.of(notification1, notification2, notification3));

        //when
        Integer count = notificationService.readAllNotifications(receiver.getEmail());

        //then
        assertThat(count).isEqualTo(2);
    }
}