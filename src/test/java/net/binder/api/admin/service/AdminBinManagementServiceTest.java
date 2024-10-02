package net.binder.api.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import net.binder.api.admin.dto.AdminBinUpdateRequest;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.entity.BinModificationStatus;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.entity.BinRegistrationStatus;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinModificationRepository;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
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
    @Autowired
    private BinModificationRepository binModificationRepository;

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
        AdminBinUpdateRequest adminBinUpdateRequest = new AdminBinUpdateRequest("title2", "address2", BinType.CIGAR,
                "url", 100d, 20d,
                "수정", null, null);
        adminBinManagementService.updateBin(admin.getEmail(), bin.getId(), adminBinUpdateRequest);

        //then
        Bin modified = binRepository.findById(bin.getId()).get();

        assertThat(modified.getTitle()).isEqualTo(adminBinUpdateRequest.getTitle());
        assertThat(modified.getPoint().getX()).isEqualTo(adminBinUpdateRequest.getLongitude());
        assertThat(modified.getPoint().getY()).isEqualTo(adminBinUpdateRequest.getLatitude());
        assertThat(modified.getAddress()).isEqualTo(adminBinUpdateRequest.getAddress());
        assertThat(modified.getType()).isEqualTo(adminBinUpdateRequest.getType());
        assertThat(modified.getImageUrl()).isEqualTo(adminBinUpdateRequest.getImageUrl());
        assertThat(modified.getModifiedAt()).isNotNull();

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications.size()).isEqualTo(1);

        Notification notification = notifications.get(0);

        assertThat(notification.getSender()).isEqualTo(admin);
        assertThat(notification.getReceiver()).isEqualTo(user);
        assertThat(notification.getAdditionalInfo()).isEqualTo(adminBinUpdateRequest.getModificationReason());
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_MODIFIED);
    }

    @Test
    @DisplayName("관리자가 직접 쓰레기통을 수정할때 등록 요청 내역이 존재하면 승인할 수 있다.")
    void updateBin_hasRegistrationId() {
        //given
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("title1", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address1", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(user, null, BinRegistrationStatus.PENDING);
        bin.setBinRegistration(binRegistration);

        binRepository.save(bin);

        //when
        AdminBinUpdateRequest adminBinUpdateRequest = new AdminBinUpdateRequest("title2", "address2", BinType.CIGAR,
                "url", 100d, 20d,
                "수정", binRegistration.getId(), null);
        adminBinManagementService.updateBin(admin.getEmail(), bin.getId(), adminBinUpdateRequest);

        //then
        assertThat(binRegistration.getStatus()).isEqualTo(BinRegistrationStatus.APPROVED);

        Bin modified = binRepository.findById(bin.getId()).get();

        assertThat(modified.getTitle()).isEqualTo(adminBinUpdateRequest.getTitle());
        assertThat(modified.getPoint().getX()).isEqualTo(adminBinUpdateRequest.getLongitude());
        assertThat(modified.getPoint().getY()).isEqualTo(adminBinUpdateRequest.getLatitude());
        assertThat(modified.getAddress()).isEqualTo(adminBinUpdateRequest.getAddress());
        assertThat(modified.getType()).isEqualTo(adminBinUpdateRequest.getType());
        assertThat(modified.getImageUrl()).isEqualTo(adminBinUpdateRequest.getImageUrl());
        assertThat(modified.getModifiedAt()).isNotNull();

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications.size()).isEqualTo(2);

        Notification notification1 = notifications.get(0);

        assertThat(notification1.getSender()).isEqualTo(admin);
        assertThat(notification1.getReceiver()).isEqualTo(user);
        assertThat(notification1.getAdditionalInfo()).isEqualTo(adminBinUpdateRequest.getModificationReason());
        assertThat(notification1.getType()).isEqualTo(NotificationType.BIN_MODIFIED);

        Notification notification2 = notifications.get(1);
        assertThat(notification2.getSender()).isEqualTo(admin);
        assertThat(notification2.getReceiver()).isEqualTo(binRegistration.getMember());
        assertThat(notification2.getAdditionalInfo()).isNull();
        assertThat(notification2.getType()).isEqualTo(NotificationType.BIN_REGISTRATION_APPROVED);
    }

    @Test
    @DisplayName("관리자가 직접 쓰레기통을 수정할때 수정 요청 내역이 존재하면 승인할 수 있다.")
    void updateBin_hasModificationId() {
        //given
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("title1", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address1", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(user, null, BinRegistrationStatus.PENDING);
        bin.setBinRegistration(binRegistration);

        binRepository.save(bin);

        BinModification binModification = new BinModification(user, bin, "title2", "address2", BinType.CIGAR, "url",
                100d, 20d, BinModificationStatus.PENDING, "수정");
        binModificationRepository.save(binModification);

        //when
        AdminBinUpdateRequest adminBinUpdateRequest = new AdminBinUpdateRequest("title2", "address2", BinType.CIGAR,
                "url", 100d, 20d,
                "수정", null, binModification.getId());
        adminBinManagementService.updateBin(admin.getEmail(), bin.getId(), adminBinUpdateRequest);

        //then
        assertThat(binModification.getStatus()).isEqualTo(BinModificationStatus.APPROVED);

        Bin modified = binRepository.findById(bin.getId()).get();

        assertThat(modified.getTitle()).isEqualTo(adminBinUpdateRequest.getTitle());
        assertThat(modified.getPoint().getX()).isEqualTo(adminBinUpdateRequest.getLongitude());
        assertThat(modified.getPoint().getY()).isEqualTo(adminBinUpdateRequest.getLatitude());
        assertThat(modified.getAddress()).isEqualTo(adminBinUpdateRequest.getAddress());
        assertThat(modified.getType()).isEqualTo(adminBinUpdateRequest.getType());
        assertThat(modified.getImageUrl()).isEqualTo(adminBinUpdateRequest.getImageUrl());
        assertThat(modified.getModifiedAt()).isNotNull();

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications.size()).isEqualTo(2);

        Notification notification1 = notifications.get(0);

        assertThat(notification1.getSender()).isEqualTo(admin);
        assertThat(notification1.getReceiver()).isEqualTo(user);
        assertThat(notification1.getAdditionalInfo()).isEqualTo(adminBinUpdateRequest.getModificationReason());
        assertThat(notification1.getType()).isEqualTo(NotificationType.BIN_MODIFIED);

        Notification notification2 = notifications.get(1);
        assertThat(notification2.getSender()).isEqualTo(admin);
        assertThat(notification2.getReceiver()).isEqualTo(binModification.getMember());
        assertThat(notification2.getAdditionalInfo()).isNull();
        assertThat(notification2.getType()).isEqualTo(NotificationType.BIN_MODIFICATION_APPROVED);
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