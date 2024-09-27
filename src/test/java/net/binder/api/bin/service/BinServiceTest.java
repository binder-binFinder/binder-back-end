package net.binder.api.bin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.binder.api.bin.dto.BinCreateRequest;
import net.binder.api.bin.dto.BinDetailResponse;
import net.binder.api.bin.dto.BinInfoForMember;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.bookmark.repository.BookmarkRepository;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.complaint.repository.ComplaintRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.memberlikebin.entity.MemberLikeBin;
import net.binder.api.memberlikebin.repository.MemberLikeBinRepository;
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
    @Autowired
    private ComplaintRepository complaintRepository;
    @Autowired
    private MemberLikeBinRepository memberLikeBinRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;

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

    @Test
    @DisplayName("비로그인 유저 쓰레기통 상세 조회")
    void getBin_NotMember() {
        //given
        Bin bin = new Bin("쓰레기통", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "주소", 5L, 10L, 2L, "image.jpg", null);
        binRepository.save(bin);

        Complaint complaint1 = new Complaint(bin, ComplaintStatus.PENDING, 3L);
        Complaint complaint2 = new Complaint(bin, ComplaintStatus.REJECTED, 5L);
        Complaint complaint3 = new Complaint(bin, ComplaintStatus.APPROVED, 10L);
        complaintRepository.saveAll(List.of(complaint1, complaint2, complaint3));

        //when
        BinDetailResponse binDetail = binService.getBinDetail(null, bin.getId());

        //then
        assertThat(binDetail.getTitle()).isEqualTo("쓰레기통");
        assertThat(binDetail.getType()).isEqualTo(BinType.GENERAL);
        assertThat(binDetail.getLongitude()).isEqualTo(100d);
        assertThat(binDetail.getLatitude()).isEqualTo(10d);
        assertThat(binDetail.getAddress()).isEqualTo("주소");
        assertThat(binDetail.getLikeCount()).isEqualTo(5);
        assertThat(binDetail.getDislikeCount()).isEqualTo(10);
        assertThat(binDetail.getBookmarkCount()).isEqualTo(2);
        assertThat(binDetail.getComplaintCount()).isEqualTo(3);
        assertThat(binDetail.getImageUrl()).isEqualTo("image.jpg");
        assertThat(binDetail.getBinInfoForMember()).isNull();
    }

    @Test
    @DisplayName("로그인 유저 쓰레기통 상세 조회")
    void getBin_Member() {
        //given
        Bin bin = new Bin("쓰레기통", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "주소", 5L, 10L, 2L, "image.jpg", null);
        bin.setBinRegistration(new BinRegistration(member, bin, BinRegistrationStatus.APPROVED));
        binRepository.save(bin);

        Complaint complaint1 = new Complaint(bin, ComplaintStatus.PENDING, 3L);
        Complaint complaint2 = new Complaint(bin, ComplaintStatus.REJECTED, 5L);
        Complaint complaint3 = new Complaint(bin, ComplaintStatus.APPROVED, 10L);
        complaintRepository.saveAll(List.of(complaint1, complaint2, complaint3));

        memberLikeBinRepository.save(new MemberLikeBin(member, bin));

        bookmarkRepository.save(new Bookmark(member, bin));

        //when
        BinDetailResponse binDetail = binService.getBinDetail(member.getEmail(), bin.getId());

        //then
        assertThat(binDetail.getTitle()).isEqualTo("쓰레기통");
        assertThat(binDetail.getType()).isEqualTo(BinType.GENERAL);
        assertThat(binDetail.getLongitude()).isEqualTo(100d);
        assertThat(binDetail.getLatitude()).isEqualTo(10d);
        assertThat(binDetail.getAddress()).isEqualTo("주소");
        assertThat(binDetail.getLikeCount()).isEqualTo(5);
        assertThat(binDetail.getDislikeCount()).isEqualTo(10);
        assertThat(binDetail.getBookmarkCount()).isEqualTo(2);
        assertThat(binDetail.getComplaintCount()).isEqualTo(3);
        assertThat(binDetail.getImageUrl()).isEqualTo("image.jpg");
        assertThat(binDetail.getBinInfoForMember()).isNotNull();

        BinInfoForMember binInfoForMember = binDetail.getBinInfoForMember();
        assertThat(binInfoForMember.getIsBookMarked()).isTrue();
        assertThat(binInfoForMember.getIsOwner()).isTrue();
        assertThat(binInfoForMember.getIsLiked()).isTrue();
        assertThat(binInfoForMember.getIsDisliked()).isFalse();
    }
}