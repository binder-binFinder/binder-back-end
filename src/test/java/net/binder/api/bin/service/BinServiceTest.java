package net.binder.api.bin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.binder.api.bin.dto.BinCreateRequest;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
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
class BinServiceTest {

    @Autowired
    private BinService binService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        String email = "test@email.com";
        member = new Member(email, "nickname", Role.ROLE_USER, null);

        memberRepository.save(member);
    }

    @Test
    @DisplayName("쓰레기통 등록 요청을 할 수 있으며 등록 상태는 PENDING이다.")
    void requestBinRegistration() {

        //given
        BinCreateRequest binCreateRequest = new BinCreateRequest("title", "address", BinType.BEVERAGE, "image.img",
                30.0,
                150.0);

        //when
        binService.requestBinRegistration(binCreateRequest, member.getEmail());

        //then
        List<Bin> bins = binRepository.findAll();
        assertThat(bins.size()).isEqualTo(1);
        Bin bin = bins.get(0);

        assertThat(bin.getPoint().getX()).isEqualTo(150);
        assertThat(bin.getPoint().getY()).isEqualTo(30);
        assertThat(bin.getBinRegistration().getMember()).isEqualTo(member);
        assertThat(bin.getBinRegistration().getStatus()).isEqualTo(BinRegistrationStatus.PENDING);
    }
}