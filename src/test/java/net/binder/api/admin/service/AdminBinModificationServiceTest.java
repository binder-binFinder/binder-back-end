package net.binder.api.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.binder.api.admin.dto.BinModificationDetail;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.entity.BinModificationStatus;
import net.binder.api.bin.repository.BinModificationRepository;
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
class AdminBinModificationServiceTest {

    @Autowired
    private AdminBinModificationService adminBinModificationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private BinModificationRepository binModificationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private BinModification binModification;

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

        binModification = new BinModification(user, bin, "title", "adress", BinType.BEVERAGE, null, 0, 10,
                BinModificationStatus.PENDING, "reason1");
        binModificationRepository.save(binModification);
    }

    @Test
    @DisplayName("쓰레기통 수정 요청을 승인하면 상태가 approved로 변경되고 알림이 전송된다.")
    void approveModification() {
        //when
        adminBinModificationService.approveModification(email, binModification.getId());

        //then
        assertThat(binModification.getStatus()).isEqualTo(BinModificationStatus.APPROVED);
        assertThat(binModification.getBin().getDeletedAt()).isNull();
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        Notification notification = notificationRepository.findAll().get(0);
        assertThat(notification.getBin()).isEqualTo(bin);
        assertThat(notification.getSender()).isEqualTo(admin);
        assertThat(notification.getReceiver()).isEqualTo(user);
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_MODIFICATION_APPROVED);
    }

    @Test
    @DisplayName("쓰레기통 수정 요청을 거절하면 상태가 rejected로 변경되고 알림이 전송된다.")
    void rejectModification() {
        //when
        String rejectReason = "거절 사유";
        adminBinModificationService.rejectModification(email, binModification.getId(), rejectReason);

        //then
        assertThat(binModification.getStatus()).isEqualTo(BinModificationStatus.REJECTED);
        assertThat(bin.getDeletedAt()).isNull();
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        Notification notification = notificationRepository.findAll().get(0);
        assertThat(notification.getBin()).isEqualTo(bin);
        assertThat(notification.getSender()).isEqualTo(admin);
        assertThat(notification.getReceiver()).isEqualTo(user);
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_MODIFICATION_REJECTED);
    }

    @Test
    @DisplayName("수정 요청 전체를 최근순으로 조회할 수 있다.")
    void getBinModificationDetails_ENTIRE() {
        //given
        Bin bin2 = new Bin("title2", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address2", 0L, 0L, 0L, null,
                null);
        binRepository.save(bin2);

        BinModification binModification2 = new BinModification(user, bin2, "title1", "address", BinType.RECYCLE, null,
                20, 10, BinModificationStatus.APPROVED, "reason2");
        binModificationRepository.save(binModification2);

        //when
        List<BinModificationDetail> binModificationDetails = adminBinModificationService.getBinModificationDetails(
                ModificationFilter.ENTIRE);

        //then
        assertThat(binModificationDetails.size()).isEqualTo(2);
        assertThat(binModificationDetails).extracting("ModificationId")
                .containsExactly(binModification2.getId(), binModification.getId());
        assertThat(binModificationDetails).extracting(BinModificationDetail::getModificationReason)
                .containsExactly("reason2", "reason1");
    }

    @Test
    @DisplayName("처리되지 않은 수정 요청을 최근순으로 조회할 수 있다.")
    void getBinModificationDetails_PENDING() {
        //given
        Bin bin2 = new Bin("title2", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address2", 0L, 0L, 0L, null,
                null);
        binRepository.save(bin2);

        BinModification binModification2 = new BinModification(user, bin2, "title1", "address", BinType.RECYCLE, null,
                20, 10, BinModificationStatus.APPROVED, null);
        binModificationRepository.save(binModification2);

        Bin bin3 = new Bin("title3", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address3", 0L, 0L, 0L, null,
                null);
        binRepository.save(bin3);

        BinModification binModification3 = new BinModification(user, bin2, "title1", "address", BinType.RECYCLE, null,
                20, 10, BinModificationStatus.PENDING, null);
        binModificationRepository.save(binModification3);

        //when
        List<BinModificationDetail> binModificationDetails = adminBinModificationService.getBinModificationDetails(
                ModificationFilter.PENDING);

        //then
        assertThat(binModificationDetails.size()).isEqualTo(2);
        assertThat(binModificationDetails).extracting("ModificationId")
                .containsExactly(binModification3.getId(), binModification.getId());
    }

    @Test
    @DisplayName("처리완료된 수정 요청을 최근순으로 조회할 수 있다.")
    void getBinModificationDetails_FINISHED() {
        //given
        Bin bin2 = new Bin("title2", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address2", 0L, 0L, 0L, null,
                null);
        binRepository.save(bin2);

        BinModification binModification2 = new BinModification(user, bin2, "title1", "address", BinType.RECYCLE, null,
                20, 10, BinModificationStatus.APPROVED, null);
        binModificationRepository.save(binModification2);

        Bin bin3 = new Bin("title3", BinType.RECYCLE, PointUtil.getPoint(127.2, 37.5), "address3", 0L, 0L, 0L, null,
                null);
        binRepository.save(bin3);

        BinModification binModification3 = new BinModification(user, bin2, "title1", "address", BinType.RECYCLE, null,
                20, 10, BinModificationStatus.REJECTED, null);
        binModificationRepository.save(binModification3);

        //when
        List<BinModificationDetail> binModificationDetails = adminBinModificationService.getBinModificationDetails(
                ModificationFilter.FINISHED);

        //then
        assertThat(binModificationDetails.size()).isEqualTo(2);
        assertThat(binModificationDetails).extracting("ModificationId")
                .containsExactly(binModification3.getId(), binModification2.getId());
    }
}