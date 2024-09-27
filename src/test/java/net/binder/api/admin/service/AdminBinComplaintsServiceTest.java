package net.binder.api.admin.service;

import static net.binder.api.notification.entity.NotificationType.BIN_COMPLAINT_APPROVED;
import static net.binder.api.notification.entity.NotificationType.BIN_DELETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.dto.TypeCount;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintInfo;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.complaint.entity.ComplaintType;
import net.binder.api.complaint.repository.ComplaintInfoRepository;
import net.binder.api.complaint.repository.ComplaintRepository;
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

@Transactional
@SpringBootTest
class AdminBinComplaintsServiceTest {

    @Autowired
    AdminBinComplaintsService adminBinComplaintsService;

    @Autowired
    ComplaintRepository complaintRepository;

    @Autowired
    ComplaintInfoRepository complaintInfoRepository;

    @Autowired
    BinRepository binRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private ComplaintInfo latest1;

    private ComplaintInfo latest2;

    private ComplaintInfo latest3;

    private Bin bin;

    private Complaint complaint1;

    private Complaint complaint2;

    private Complaint complaint3;
    @Autowired
    private BinRegistrationRepository binRegistrationRepository;

    @BeforeEach
    void setUp() {
        bin = new Bin("title", BinType.CIGAR, PointUtil.getPoint(10d, 10d), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        complaint1 = new Complaint(bin, ComplaintStatus.PENDING, 3L);
        complaint2 = new Complaint(bin, ComplaintStatus.REJECTED, 3L);
        complaint3 = new Complaint(bin, ComplaintStatus.APPROVED, 3L);
        complaintRepository.saveAll(List.of(complaint1, complaint2, complaint3));

        ComplaintInfo complaintInfo1 = new ComplaintInfo(complaint1, null, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo2 = new ComplaintInfo(complaint1, null, ComplaintType.INVALID_NAME);
        latest1 = new ComplaintInfo(complaint1, null, ComplaintType.INVALID_LOCATION);
        complaintInfoRepository.saveAll(List.of(complaintInfo1, complaintInfo2, latest1));

        ComplaintInfo complaintInfo3 = new ComplaintInfo(complaint2, null, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo4 = new ComplaintInfo(complaint2, null, ComplaintType.INVALID_NAME);
        latest2 = new ComplaintInfo(complaint2, null, ComplaintType.INVALID_LOCATION);
        complaintInfoRepository.saveAll(List.of(complaintInfo3, complaintInfo4, latest2));

        ComplaintInfo complaintInfo5 = new ComplaintInfo(complaint3, null, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo6 = new ComplaintInfo(complaint3, null, ComplaintType.INVALID_NAME);
        latest3 = new ComplaintInfo(complaint3, null, ComplaintType.INVALID_LOCATION);
        complaintInfoRepository.saveAll(List.of(complaintInfo5, complaintInfo6, latest3));
    }

    @Test
    @DisplayName("신고 내역 조회시 카운트가 3개 이상인 신고 내역만 표시된다.")
    void getBinComplaintDetails() {
        //given
        Complaint complaint4 = new Complaint(bin, ComplaintStatus.PENDING, 2L);
        complaintRepository.save(complaint4);

        ComplaintInfo complaintInfo7 = new ComplaintInfo(complaint4, null, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo8 = new ComplaintInfo(complaint4, null, ComplaintType.INVALID_NAME);
        complaintInfoRepository.saveAll(List.of(complaintInfo7, complaintInfo8));

        //when
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(
                ComplaintFilter.ENTIRE);

        //then
        assertThat(binComplaintDetails).extracting(BinComplaintDetail::getComplaintId)
                .containsExactly(complaint3.getId(), complaint2.getId(), complaint1.getId());
    }


    @Test
    @DisplayName("심사 목록을 조회하면 각각 신고에는 가장 최근의 신고 날짜가 반영된다.")
    void getBinComplaintDetails_has_latestCreatedAt() {
        //when
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(
                ComplaintFilter.ENTIRE);
        BinComplaintDetail result = binComplaintDetails.stream()
                .filter(binComplaintDetail -> binComplaintDetail.getComplaintId().equals(complaint1.getId())).findAny()
                .get();
        //then

        assertThat(result.getMostRecentComplaintAt()).isCloseTo(latest1.getCreatedAt(), within(1, ChronoUnit.SECONDS));

    }

    @Test
    @DisplayName("아직 심사 중인 신고 내역만을 조회할 수 있다.")
    void getBinComplaintDetails_Pending() {

        //when
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(
                ComplaintFilter.PENDING);

        //then
        assertThat(binComplaintDetails).extracting(BinComplaintDetail::getComplaintId)
                .containsExactly(complaint1.getId());

    }

    @Test
    @DisplayName("심사가 끝난 신고 내역만을 조회할 수 있다.")
    void getBinComplaintDetails_finished() {

        //when
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(
                ComplaintFilter.FINISHED);

        //then
        assertThat(binComplaintDetails).extracting(BinComplaintDetail::getComplaintId)
                .containsExactly(complaint3.getId(), complaint2.getId());
    }

    @Test
    @DisplayName("신고 카운트가 3 이상이고 Pending인 심사의 개수를 확인할 수 있다.")
    void getComplaintPendingCount() {
        //given
        Complaint complaint4 = new Complaint(bin, ComplaintStatus.PENDING, 2L);
        complaintRepository.save(complaint4);

        ComplaintInfo complaintInfo7 = new ComplaintInfo(complaint4, null, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo8 = new ComplaintInfo(complaint4, null, ComplaintType.INVALID_NAME);
        complaintInfoRepository.saveAll(List.of(complaintInfo7, complaintInfo8));

        //when
        Long complaintPendingCount = adminBinComplaintsService.getComplaintPendingCount();

        //then
        assertThat(complaintPendingCount).isEqualTo(1);
    }

    @Test
    @DisplayName("신고 id에 따른 신고 타입별 카운트를 확인할 수 있다.")
    void getBinComplaintCountsPerType() {

        ComplaintInfo added1 = new ComplaintInfo(complaint1, null, ComplaintType.IS_PRIVATE);
        ComplaintInfo added2 = new ComplaintInfo(complaint1, null, ComplaintType.IS_PRIVATE);
        ComplaintInfo added3 = new ComplaintInfo(complaint1, null, ComplaintType.INVALID_NAME);
        complaintInfoRepository.saveAll(List.of(added1, added2, added3));

        List<TypeCount> typeCounts = adminBinComplaintsService.getBinComplaintCountsPerType(complaint1.getId());

        assertThat(typeCounts).extracting(TypeCount::getType)
                .containsExactly(ComplaintType.IS_PRIVATE, ComplaintType.INVALID_NAME, ComplaintType.INVALID_LOCATION);

        assertThat(typeCounts).extracting(TypeCount::getCount)
                .containsExactly(3L, 2L, 1L);
    }

    @Test
    @DisplayName("신고가 승인되면 쓰레기통이 softDelete되고 status가 APPROVED로 변경되며, 신고를 작성한 모든 사람들 및 쓰레기통 등록자에게 알림이 전송된다.")
    void approve() {
        //given
        String adminEmail = "admin@example.com";
        String user1Email = "user1@example.com";
        String user2Email = "user2@example.com";
        String user3Email = "user3@example.com";
        String user4Email = "user4@example.com";

        Member admin = new Member(adminEmail, "admin", Role.ROLE_ADMIN, null);
        Member user1 = new Member(user1Email, "user1", Role.ROLE_USER, null);
        Member user2 = new Member(user2Email, "user2", Role.ROLE_USER, null);
        Member user3 = new Member(user3Email, "user3", Role.ROLE_USER, null);
        Member user4 = new Member(user4Email, "user4", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user1, user2, user3, user4));

        bin.setBinRegistration(new BinRegistration(user4, bin, BinRegistrationStatus.APPROVED));

        Complaint complaint = new Complaint(bin, ComplaintStatus.PENDING, 3L);
        complaintRepository.save(complaint);

        ComplaintInfo complaintInfo1 = new ComplaintInfo(complaint, user1, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo2 = new ComplaintInfo(complaint, user2, ComplaintType.INVALID_NAME);
        ComplaintInfo complaintInfo3 = new ComplaintInfo(complaint, user3, ComplaintType.INVALID_NAME);

        complaintInfoRepository.saveAll(List.of(complaintInfo1, complaintInfo2, complaintInfo3));

        //when
        adminBinComplaintsService.approve(adminEmail, complaint.getId(), "이름이 잘못됐습니다.");

        //then
        assertThat(complaint.getBin().getDeletedAt()).isNotNull();
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.APPROVED);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications.size()).isEqualTo(4);
        assertThat(notifications).extracting(Notification::getBin).allMatch(bin -> bin.equals(this.bin));
        assertThat(notifications).extracting(Notification::getType)
                .containsExactly(BIN_COMPLAINT_APPROVED, BIN_COMPLAINT_APPROVED, BIN_COMPLAINT_APPROVED, BIN_DELETED);
        assertThat(notifications).extracting(Notification::getSender).allMatch(member -> member.equals(admin));
        assertThat(notifications).extracting(Notification::getReceiver).containsExactly(user1, user2, user3, user4);
    }

    @Test
    @DisplayName("신고가 거절되면 status가 REJECTED로 변경되며, 로그 목적의 알림이 생성된다.")
    void reject() {
        //given
        String adminEmail = "admin@example.com";
        String user1Email = "user1@example.com";
        String user2Email = "user2@example.com";
        String user3Email = "user3@example.com";

        Member admin = new Member(adminEmail, "admin", Role.ROLE_ADMIN, null);
        Member user1 = new Member(user1Email, "user1", Role.ROLE_USER, null);
        Member user2 = new Member(user2Email, "user2", Role.ROLE_USER, null);
        Member user3 = new Member(user3Email, "user3", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user1, user2, user3));

        Complaint complaint = new Complaint(bin, ComplaintStatus.PENDING, 3L);
        complaintRepository.save(complaint);

        ComplaintInfo complaintInfo1 = new ComplaintInfo(complaint, user1, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo2 = new ComplaintInfo(complaint, user2, ComplaintType.INVALID_NAME);
        ComplaintInfo complaintInfo3 = new ComplaintInfo(complaint, user3, ComplaintType.INVALID_NAME);

        complaintInfoRepository.saveAll(List.of(complaintInfo1, complaintInfo2, complaintInfo3));

        //when
        adminBinComplaintsService.reject(adminEmail, complaint.getId(), "신고가 적절하지 않습니다.");

        //then
        assertThat(complaint.getBin().getDeletedAt()).isNull();
        assertThat(complaint.getStatus()).isEqualTo(ComplaintStatus.REJECTED);

        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications.size()).isEqualTo(1);
        assertThat(notifications).extracting(Notification::getBin).allMatch(bin -> bin.equals(this.bin));
        assertThat(notifications).extracting(Notification::getType)
                .allMatch(type -> type == NotificationType.BIN_COMPLAINT_REJECTED);
        assertThat(notifications).extracting(Notification::getSender).allMatch(member -> member.equals(admin));
        assertThat(notifications).extracting(Notification::getReceiver).allMatch(Objects::isNull);
    }

    @Test
    @DisplayName("심사중인 신고가 아니라면 거절시 예외가 발생한다.")
    void reject_fail_isNotPending() {
        //given
        String adminEmail = "admin@example.com";
        String user1Email = "user1@example.com";
        String user2Email = "user2@example.com";
        String user3Email = "user3@example.com";

        Member admin = new Member(adminEmail, "admin", Role.ROLE_ADMIN, null);
        Member user1 = new Member(user1Email, "user1", Role.ROLE_USER, null);
        Member user2 = new Member(user2Email, "user2", Role.ROLE_USER, null);
        Member user3 = new Member(user3Email, "user3", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user1, user2, user3));

        Complaint complaint = new Complaint(bin, ComplaintStatus.APPROVED, 3L);
        complaintRepository.save(complaint);

        ComplaintInfo complaintInfo1 = new ComplaintInfo(complaint, user1, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo2 = new ComplaintInfo(complaint, user2, ComplaintType.INVALID_NAME);
        ComplaintInfo complaintInfo3 = new ComplaintInfo(complaint, user3, ComplaintType.INVALID_NAME);

        complaintInfoRepository.saveAll(List.of(complaintInfo1, complaintInfo2, complaintInfo3));

        //when & then
        assertThatThrownBy(() -> adminBinComplaintsService.reject(adminEmail, complaint.getId(), "신고가 적절하지 않습니다."))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("심사중인 신고가 아니라면 승인시 예외가 발생한다.")
    void approve_fail_isNotPending() {
        //given
        String adminEmail = "admin@example.com";
        String user1Email = "user1@example.com";
        String user2Email = "user2@example.com";
        String user3Email = "user3@example.com";

        Member admin = new Member(adminEmail, "admin", Role.ROLE_ADMIN, null);
        Member user1 = new Member(user1Email, "user1", Role.ROLE_USER, null);
        Member user2 = new Member(user2Email, "user2", Role.ROLE_USER, null);
        Member user3 = new Member(user3Email, "user3", Role.ROLE_USER, null);

        memberRepository.saveAll(List.of(admin, user1, user2, user3));

        Complaint complaint = new Complaint(bin, ComplaintStatus.APPROVED, 3L);
        complaintRepository.save(complaint);

        ComplaintInfo complaintInfo1 = new ComplaintInfo(complaint, user1, ComplaintType.IS_PRIVATE);
        ComplaintInfo complaintInfo2 = new ComplaintInfo(complaint, user2, ComplaintType.INVALID_NAME);
        ComplaintInfo complaintInfo3 = new ComplaintInfo(complaint, user3, ComplaintType.INVALID_NAME);

        complaintInfoRepository.saveAll(List.of(complaintInfo1, complaintInfo2, complaintInfo3));

        //when & then
        assertThatThrownBy(() -> adminBinComplaintsService.approve(adminEmail, complaint.getId(), "신고가 적절하지 않습니다."))
                .isInstanceOf(BadRequestException.class);
    }
}