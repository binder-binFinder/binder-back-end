package net.binder.api.complaint.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.complaint.entity.ComplaintType;
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

    private Member member;

    private Bin bin;

    @BeforeEach
    void setUp() {
        String email = "test@email.com";
        member = new Member(email, "nick", Role.ROLE_USER, null);
        memberRepository.save(member);

        bin = new Bin("title", BinType.CIGAR, null, "address", 0L, 0L, 0L, null, null);
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
}