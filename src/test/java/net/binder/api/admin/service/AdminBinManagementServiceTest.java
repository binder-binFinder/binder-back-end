package net.binder.api.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import net.binder.api.bin.dto.BinUpdateRequest;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AdminBinManagementServiceTest {

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AdminBinManagementService adminBinManagementService;

    @Autowired
    EntityManager entityManager;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    @DisplayName("관리자는 쓰레기통을 직접 수정할 수 있고 쓰레기통의 주인에게 알림이 전송된다.")
    void updateBin() {
        //given
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("title1", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address1", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(user, null, BinRegistrationStatus.PENDING);
        bin.setBinRegistration(binRegistration);

        binRepository.save(bin);

        //when
        BinUpdateRequest binUpdateRequest = new BinUpdateRequest("title2", "address2", BinType.CIGAR, "url", 100d, 20d,
                "수정");
        adminBinManagementService.updateBin(admin.getEmail(), bin.getId(), binUpdateRequest);

        //then
        Bin modified = binRepository.findById(bin.getId()).get();

        assertThat(modified.getTitle()).isEqualTo(binUpdateRequest.getTitle());
        assertThat(modified.getPoint().getX()).isEqualTo(binUpdateRequest.getLongitude());
        assertThat(modified.getPoint().getY()).isEqualTo(binUpdateRequest.getLatitude());
        assertThat(modified.getAddress()).isEqualTo(binUpdateRequest.getAddress());
        assertThat(modified.getType()).isEqualTo(binUpdateRequest.getType());
        assertThat(modified.getImageUrl()).isEqualTo(binUpdateRequest.getImageUrl());
        assertThat(modified.getModifiedAt()).isNotNull();

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications.size()).isEqualTo(1);

        Notification notification = notifications.get(0);

        assertThat(notification.getSender()).isEqualTo(admin);
        assertThat(notification.getReceiver()).isEqualTo(user);
        assertThat(notification.getAdditionalInfo()).isEqualTo(binUpdateRequest.getModificationReason());
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_MODIFIED);
    }

    @Test
    @DisplayName("쓰레기통을 softDelete 할 수 있고 주인에게 알림이 전송된다.")
    void deleteBin() {
        //given
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("title1", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address1", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(user, null, BinRegistrationStatus.PENDING);
        bin.setBinRegistration(binRegistration);

        binRepository.save(bin);

        //when
        adminBinManagementService.deleteBin(admin.getEmail(), bin.getId(), "잘못된장소");

        //then
        assertThat(bin.getDeletedAt()).isNotNull();

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications.size()).isEqualTo(1);

        Notification notification = notifications.get(0);

        assertThat(notification.getSender()).isEqualTo(admin);
        assertThat(notification.getReceiver()).isEqualTo(user);
        assertThat(notification.getAdditionalInfo()).isEqualTo("잘못된장소");
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_DELETED);
    }
}