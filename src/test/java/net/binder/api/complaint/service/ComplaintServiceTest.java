package net.binder.api.complaint.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintInfo;
import net.binder.api.complaint.entity.ComplaintType;
import net.binder.api.complaint.repository.ComplaintInfoRepository;
import net.binder.api.complaint.repository.ComplaintRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ComplaintServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintInfoRepository complaintInfoRepository;

    private Member member;

    private Bin bin;

    @BeforeEach
    void setUp() {
        String email = "test@email.com";
        member = new Member(email, "nick", Role.ROLE_USER, null);
        memberRepository.save(member);

        bin = new Bin("title", BinType.CIGAR, PointUtil.getPoint(127.2, 37.5), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
    }

    @Test
    @DisplayName("동일한 유저가 동일한 쓰레기통에 대해 같은 타입의 신고가 존재하면 예외가 발생한다.")
    void createComplaint_duplicate() {

        //given
        complaintService.createComplaint(member.getEmail(), bin.getId(), ComplaintType.INVALID_LOCATION);

        //when & then
        assertThatThrownBy(() -> complaintService.createComplaint(member.getEmail(), bin.getId(),
                ComplaintType.INVALID_LOCATION))
                .isInstanceOf(BadRequestException.class); // 같은 조건으로 신고
    }

    @Test
    @DisplayName("동일한 유저가 동일한 쓰레기통에 대해 다른 타입의 신고를 할 수 있다.")
    void createComplaint_differentType() {

        //given
        complaintService.createComplaint(member.getEmail(), bin.getId(), ComplaintType.INVALID_LOCATION);

        //when & then
        assertThatCode(() -> complaintService.createComplaint(member.getEmail(), bin.getId(), ComplaintType.IS_PRIVATE))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("기존에 신고 내역이 없다면 새로운 신고 카드를 생성한다.")
    void createComplaint_new() {
        //when
        Complaint complaint = complaintService.createComplaint(member.getEmail(), bin.getId(),
                ComplaintType.INVALID_LOCATION);

        //then
        List<ComplaintInfo> complaintInfos = complaintInfoRepository.findAllByComplaintId(complaint.getId());
        assertThat(complaint.getCount()).isEqualTo(1);
        assertThat(complaintInfos.size()).isEqualTo(1);
        assertThat(complaintInfos.get(0).getType()).isEqualTo(ComplaintType.INVALID_LOCATION);
        assertThat(complaintInfos.get(0).getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("기존에 신고 카드가 존재하면 신고 내역을 추가한다.")
    void createComplaint_old() {
        //given
        Complaint complaint1 = complaintService.createComplaint(member.getEmail(), bin.getId(),
                ComplaintType.INVALID_LOCATION);

        //when
        Complaint complaint2 = complaintService.createComplaint(member.getEmail(), bin.getId(),
                ComplaintType.INVALID_NAME);

        //then
        assertThat(complaint1).isEqualTo(complaint2);
        List<ComplaintInfo> complaintInfos = complaintInfoRepository.findAllByComplaintId(complaint1.getId());
        assertThat(complaint1.getCount()).isEqualTo(2);
        assertThat(complaintInfos.size()).isEqualTo(2);
        assertThat(complaintInfos).extracting(ComplaintInfo::getType)
                .containsExactly(ComplaintType.INVALID_LOCATION, ComplaintType.INVALID_NAME);
    }

    @Test
    @DisplayName("기존에 신고 카드가 존재하지만 심사가 끝났다면 신고 카드를 새로 생성한다.")
    void createComplaint_old_approved() {
        //given
        Complaint complaint1 = complaintService.createComplaint(member.getEmail(), bin.getId(),
                ComplaintType.INVALID_LOCATION);
        complaint1.reject();

        //when
        Complaint complaint2 = complaintService.createComplaint(member.getEmail(), bin.getId(),
                ComplaintType.INVALID_NAME);

        //then
        assertThat(complaint1).isNotEqualTo(complaint2);
        List<ComplaintInfo> complaintInfos = complaintInfoRepository.findAllByComplaintId(complaint2.getId());
        assertThat(complaint2.getCount()).isEqualTo(1);
        assertThat(complaintInfos.size()).isEqualTo(1);
        assertThat(complaintInfos).extracting(ComplaintInfo::getType)
                .containsExactly(ComplaintType.INVALID_NAME);
    }
}