package net.binder.api.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.binder.api.admin.dto.BinRegistrationDetail;
import net.binder.api.admin.dto.RegistrationFilter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
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
class AdminBinRegistrationServiceTest {

    @Autowired
    private AdminBinRegistrationService adminBinRegistrationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private BinRegistrationRepository binRegistrationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private BinRegistration binRegistration;

    private Bin bin;

    private Member admin;

    private Member user;

    private static final String email = "admin@email.com";

    @BeforeEach
    void setUp() {
        admin = new Member(email, "admin", Role.ROLE_ADMIN, null);
        user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        bin = new Bin("title", BinType.BEVERAGE, PointUtil.getPoint(127.2, 37.5), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
    }

    @Test
    @DisplayName("쓰레기통 등록 요청을 승인하면 상태가 approved로 변경되고 알림이 전송된다.")
    void approveRegistration() {
        //when
        adminBinRegistrationService.approveRegistration(email, binRegistration.getId());

        //then
        assertThat(binRegistration.getStatus()).isEqualTo(BinRegistrationStatus.APPROVED);
        assertThat(binRegistration.getBin().getDeletedAt()).isNull();
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        Notification notification = notificationRepository.findAll().get(0);
        assertThat(notification.getBin()).isEqualTo(bin);
        assertThat(notification.getSender()).isEqualTo(admin);
        assertThat(notification.getReceiver()).isEqualTo(user);
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_REGISTRATION_APPROVED);
    }

    @Test
    @DisplayName("쓰레기통 등록 요청을 거절하면 상태가 rejected로 변경되고, bin이 softDelete되며, 알림이 전송된다.")
    void rejectRegistration() {
        //when
        String rejectReason = "거절 사유";
        adminBinRegistrationService.rejectRegistration(email, binRegistration.getId(), rejectReason);

        //then
        assertThat(binRegistration.getStatus()).isEqualTo(BinRegistrationStatus.REJECTED);
        assertThat(bin.getDeletedAt()).isNotNull();
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        Notification notification = notificationRepository.findAll().get(0);
        assertThat(notification.getBin()).isEqualTo(bin);
        assertThat(notification.getSender()).isEqualTo(admin);
        assertThat(notification.getReceiver()).isEqualTo(user);
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_REGISTRATION_REJECTED);
    }

    @Test
    @DisplayName("등록 요청 전체를 최근순으로 조회할 수 있다.")
    void getBinRegistrationDetails_ENTIRE() {
        //given
        Bin bin2 = new Bin("title2", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);

        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.APPROVED);
        binRegistrationRepository.save(binRegistration2);

        //when
        List<BinRegistrationDetail> binRegistrationDetails = adminBinRegistrationService.getBinRegistrationDetails(
                RegistrationFilter.ENTIRE);

        //then
        assertThat(binRegistrationDetails.size()).isEqualTo(2);
        assertThat(binRegistrationDetails).extracting("registrationId")
                .containsExactly(binRegistration2.getId(), binRegistration.getId());
    }

    @Test
    @DisplayName("처리되지 않은 등록 요청을 최근순으로 조회할 수 있다.")
    void getBinRegistrationDetails_PENDING() {
        //given
        Bin bin2 = new Bin("title2", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);

        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.APPROVED);
        binRegistrationRepository.save(binRegistration2);

        Bin bin3 = new Bin("title3", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);

        BinRegistration binRegistration3 = new BinRegistration(user, bin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);

        //when
        List<BinRegistrationDetail> binRegistrationDetails = adminBinRegistrationService.getBinRegistrationDetails(
                RegistrationFilter.PENDING);

        //then
        assertThat(binRegistrationDetails.size()).isEqualTo(2);
        assertThat(binRegistrationDetails).extracting("registrationId")
                .containsExactly(binRegistration3.getId(), binRegistration.getId());
    }

    @Test
    @DisplayName("처리완료된 등록 요청을 최근순으로 조회할 수 있다.")
    void getBinRegistrationDetails_FINISHED() {
        //given
        Bin bin2 = new Bin("title2", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);

        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.APPROVED);
        binRegistrationRepository.save(binRegistration2);

        Bin bin3 = new Bin("title3", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);

        BinRegistration binRegistration3 = new BinRegistration(user, bin3, BinRegistrationStatus.REJECTED);
        binRegistrationRepository.save(binRegistration3);

        //when
        List<BinRegistrationDetail> binRegistrationDetails = adminBinRegistrationService.getBinRegistrationDetails(
                RegistrationFilter.FINISHED);

        //then
        assertThat(binRegistrationDetails.size()).isEqualTo(2);
        assertThat(binRegistrationDetails).extracting("registrationId")
                .containsExactly(binRegistration3.getId(), binRegistration2.getId());
    }
}