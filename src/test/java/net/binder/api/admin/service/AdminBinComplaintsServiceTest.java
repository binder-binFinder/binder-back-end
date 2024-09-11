package net.binder.api.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.dto.TypeCount;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintInfo;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.complaint.entity.ComplaintType;
import net.binder.api.complaint.repository.ComplaintInfoRepository;
import net.binder.api.complaint.repository.ComplaintRepository;
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

    private ComplaintInfo latest1;

    private ComplaintInfo latest2;

    private ComplaintInfo latest3;

    private Bin bin;

    private Complaint complaint1;

    @BeforeEach
    void setUp() {
        bin = new Bin("title", BinType.CIGAR, PointUtil.getPoint(10d, 10d), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        complaint1 = new Complaint(bin, ComplaintStatus.PENDING, 3L);
        Complaint complaint2 = new Complaint(bin, ComplaintStatus.REJECTED, 3L);
        Complaint complaint3 = new Complaint(bin, ComplaintStatus.APPROVED, 3L);
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
    @DisplayName("신고 내역 조회시 카운트가 3개 이상인 신고 내역만 표시되고 각 신고 상세 내역은 가장 최근 신고 날짜를 저장한다.")
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
        assertThat(binComplaintDetails).extracting(BinComplaintDetail::getMostRecentComplaintAt)
                .containsExactly(latest1.getCreatedAt(), latest2.getCreatedAt(), latest3.getCreatedAt());
    }

    @Test
    @DisplayName("아직 심사 중인 신고 내역만을 조회할 수 있다.")
    void getBinComplaintDetails_Pending() {

        //when
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(
                ComplaintFilter.FINISHED);

        //then
        assertThat(binComplaintDetails).extracting(BinComplaintDetail::getMostRecentComplaintAt)
                .containsExactly(latest2.getCreatedAt(), latest3.getCreatedAt());
    }

    @Test
    @DisplayName("심사가 끝난 신고 내역만을 조회할 수 있다.")
    void getBinComplaintDetails_finished() {

        //when
        List<BinComplaintDetail> binComplaintDetails = adminBinComplaintsService.getBinComplaintDetails(
                ComplaintFilter.PENDING);

        //then
        assertThat(binComplaintDetails).extracting(BinComplaintDetail::getMostRecentComplaintAt)
                .containsExactly(latest1.getCreatedAt());
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
}