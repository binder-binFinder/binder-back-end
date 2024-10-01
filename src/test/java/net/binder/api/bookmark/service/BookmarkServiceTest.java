package net.binder.api.bookmark.service;

import jakarta.persistence.EntityManager;
import net.binder.api.admin.service.AdminBinManagementService;
import net.binder.api.admin.service.AdminBinRegistrationService;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.service.BinService;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.bookmark.repository.BookmarkRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;

@Transactional
@SpringBootTest
class BookmarkServiceTest {

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRegistrationRepository binRegistrationRepository;

    @Autowired
    private AdminBinRegistrationService adminBinRegistrationService;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private AdminBinManagementService adminBinManagementService;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private BinService binService;

    @Autowired
    private EntityManager entityManager;

    private Member testMember;

    private Member testMember2;

    private Bin bin1;

    private Bin bin2;

    private Bin bin3;

    private Bin bin4;

    private final Offset<Double> distanceTolerance = within(0.1);

    @BeforeEach
    void setUp() {
        testMember = new Member("dusgh7031@gmail.com", "테스트", Role.ROLE_USER, "http://example.com/image.jpg");
        testMember2 = new Member("dusgh70312@gmail.com", "테스트2", Role.ROLE_USER, "http://example.com/image.jpg");
        memberRepository.save(testMember);
        memberRepository.save(testMember2);

        bin1 = new Bin("title1", BinType.CIGAR, PointUtil.getPoint(126.971969841012, 37.578567094578), "address1", 0L, 0L, 0L, null, null);
        bin2 = new Bin("title2", BinType.GENERAL, PointUtil.getPoint(126.971968136353, 37.577376610574), "address2", 0L, 0L, 0L, null, null);
        bin3 = new Bin("title3", BinType.BEVERAGE, PointUtil.getPoint(126.971968136353, 37.577376610574), "address3", 0L, 0L, 0L, null, null);
        bin4 = new Bin("title4", BinType.BEVERAGE, PointUtil.getPoint(126.97154998287, 37.579971733838), "address4", 0L, 0L, 0L, null, null);

        binRepository.save(bin1);
        binRepository.save(bin2);
        binRepository.save(bin3);
        binRepository.save(bin4);
    }

    @DisplayName("로그인한 유저는 쓰레기통을 북마크할 수 있다.")
    @Test
    void bookmarks_bin() {

        bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId());
        bookmarkService.createBookMark("dusgh7031@gmail.com", bin3.getId());
        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId(), bin3.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1", "address3");
        assertThat(bookmarks).extracting("title").containsExactly("title1", "title3");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR, BinType.BEVERAGE);

        assertThat(bookmarks).extracting("distance")
                .satisfies(distance -> {
                    assertThat((Double) distance.get(0)).isCloseTo(0.0, distanceTolerance);
                    assertThat((Double) distance.get(1)).isCloseTo(132.13127520524972, distanceTolerance);

                });
    }

    @DisplayName("북마크한 것이 없으면 목록 조회를 할 때 아무 것도 뜨지 않는다.")
    @Test
    void no_bookmarks() {

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);
        assertThat(bookmarks.size()).isEqualTo(0);
    }

    @DisplayName("이미 북마크 한 쓰레기통을 다시 북마크할 수 없다.")
    @Test
    void no_twice_bookmark() {

        bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId());

        assertThatThrownBy(() -> bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 북마크를 한 쓰레기통입니다.");


        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);
        Bin bin = binService.findById(bin1.getId());
        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(1L);

    }

    @DisplayName("북마크를 취소할 수 있다")
    @Test
    void cancel_bookmark() {

        bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);
        Bin bin = binService.findById(bin1.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(1L);

        bookmarkService.deleteBookMark("dusgh7031@gmail.com", bin1.getId());
        List<BookmarkResponse> bookmarksList = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);
        Bin b = binService.findById(bin1.getId());

        assertThat(bookmarksList.size()).isEqualTo(0);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(0L);

    }

    @DisplayName("북마크하지 않은 쓰레기통을 북마크 취소할 수 없다.")
    @Test
    void no_cancel_bookmark_bin_not_bookmarked() {

        assertThatThrownBy(() -> bookmarkService.deleteBookMark("dusgh7031@gmail.com", bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("북마크를 하지 않은 쓰레기통입니다.");
    }

    @DisplayName("북마크를 하면 쓰레기통의 북마크 수가 올라간다.")
    @Test
    void bookmark_count_up() {

        bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin1.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);
        Bin bin = binService.findById(bin1.getId());


        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(2L);
    }

    @DisplayName("북마크를 취소하면 쓰레기통의 북마크 수가 올라간다.")
    @Test
    void bookmark_count_down() {

        bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin1.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);
        Bin bin = binService.findById(bin1.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(2L);

        bookmarkService.deleteBookMark("dusgh7031@gmail.com", bin1.getId());
        bookmarkService.deleteBookMark("dusgh70312@gmail.com", bin1.getId());

        List<BookmarkResponse> bookmarks2 = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null, null);
        List<BookmarkResponse> bookmarks3 = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, null, null);

        Bin bin2 = binService.findById(bin1.getId());

        assertThat(bookmarks2.size()).isEqualTo(0);
        assertThat(bookmarks3.size()).isEqualTo(0);
        assertThat(bin2).extracting("bookmarkCount").isEqualTo(0L);
    }

    @DisplayName("북마크를 리스트를 조회하면 현재 위치에서 얼마나 떨어져있는지 거리를 확인할 수 있다.")
    @Test
    void bookmark_distance() {

        bookmarkService.createBookMark("dusgh70312@gmail.com", bin1.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin2.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin3.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin4.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, null, null);
        Bin b1 = binService.findById(bin1.getId());
        Bin b2 = binService.findById(bin2.getId());
        Bin b3 = binService.findById(bin3.getId());
        Bin b4 = binService.findById(bin4.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId(), bin2.getId(), bin3.getId(), bin4.getId());
        assertThat(bookmarks).extracting("address").containsExactly(
                "address1",
                "address2",
                "address3",
                "address4"
        );
        assertThat(bookmarks).extracting("title").containsExactly(
                "title1",
                "title2",
                "title3",
                "title4");
        assertThat(bookmarks).extracting("binType").containsExactly(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.BEVERAGE,
                BinType.BEVERAGE
        );
        assertThat(b1).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b2).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b3).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b4).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(bookmarks).extracting("distance")
                .satisfies(distance -> {
                    assertThat((Double) distance.get(0)).isCloseTo(0.0, distanceTolerance);
                    assertThat((Double) distance.get(1)).isCloseTo(132.13127520524972, distanceTolerance);
                    assertThat((Double) distance.get(2)).isCloseTo(132.13127520524972, distanceTolerance);
                    assertThat((Double) distance.get(3)).isCloseTo(160.2506317060672, distanceTolerance);

                });
    }

    @DisplayName("북마크를 리스트를 조회하면 특정 북마크부터 10개씩 받아올 수 있다.(가장 처음 북마크)")
    @Test
    void bookmark_distance_no_offset_by_first_bookmark() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin binA = saveBin(user, "쓰1", BinType.CIGAR, 126.972105810775, 37.57703219307, "a1");
        Bin binB = saveBin(user, "쓰2", BinType.CIGAR,126.97174802679, 37.576573141464, "a2");
        Bin binC = saveBin(user, "쓰3", BinType.CIGAR,126.972100023583, 37.576462042472, "a3");
        Bin binD = saveBin(user, "쓰4", BinType.CIGAR,126.970550412243, 37.576612402426, "a4");
        Bin binE = saveBin(user, "쓰5", BinType.CIGAR,126.970632289448, 37.576536739295, "a5");
        Bin binF = saveBin(user, "쓰6", BinType.CIGAR,126.96968004129, 37.577009971091, "a6");
        Bin binG = saveBin(user, "쓰7", BinType.CIGAR,126.969547804414, 37.576762163611, "a7");
        Bin binH = saveBin(user, "쓰8", BinType.CIGAR,126.969059394981, 37.577133516569, "a8");

        bookmarkService.createBookMark("dusgh70312@gmail.com", bin1.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin2.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin3.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin4.getId());

        bookmarkService.createBookMark("dusgh70312@gmail.com", binA.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binB.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binC.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binD.getId());

        bookmarkService.createBookMark("dusgh70312@gmail.com", binE.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binF.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binG.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binH.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, null, null);

        assertThat(bookmarks).hasSize(10);
        Bin b = binService.findById(bin1.getId());
        Bin b1 = binService.findById(bin2.getId());
        Bin b2 = binService.findById(bin3.getId());
        Bin b3 = binService.findById(bin4.getId());
        Bin b4 = binService.findById(binA.getId());
        Bin b5 = binService.findById(binB.getId());
        Bin b6 = binService.findById(binC.getId());
        Bin b7 = binService.findById(binD.getId());
        Bin b8 = binService.findById(binE.getId());
        Bin b9 = binService.findById(binF.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(
                b.getId(),
                b1.getId(),
                b2.getId(),
                b3.getId(),
                b4.getId(),
                b5.getId(),
                b6.getId(),
                b7.getId(),
                b8.getId(),
                b9.getId());

        assertThat(bookmarks).extracting("address").containsExactly(
                "address1",
                "address2",
                "address3",
                "address4",
                "a1",
                "a2",
                "a3",
                "a4",
                "a5",
                "a6"
        );
        assertThat(bookmarks).extracting("title").containsExactly(
                "title1",
                "title2",
                "title3",
                "title4",
                "쓰1",
                "쓰2",
                "쓰3",
                "쓰4",
                "쓰5",
                "쓰6");
        assertThat(bookmarks).extracting("binType").containsExactly(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.BEVERAGE,
                BinType.BEVERAGE,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR
        );
    }

    @DisplayName("북마크를 리스트를 조회하면 특정 북마크부터 10개씩 받아올 수 있다.")
    @Test
    void bookmark_distance_no_offset() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin binA = saveBin(user, "쓰1", BinType.CIGAR, 126.97196813635301, 37.577376610574, "a1");
        Bin binB = saveBin(user, "쓰2", BinType.CIGAR, 126.971100692714, 37.579569691324, "a2");
        Bin binC = saveBin(user, "쓰3", BinType.CIGAR, 126.97154998287, 37.579971733838, "a3");
        Bin binD = saveBin(user, "쓰4", BinType.CIGAR, 126.972105810775, 37.57703219307, "a4");
        Bin binE = saveBin(user, "쓰5", BinType.CIGAR, 126.97055041224299, 37.576612402426, "a5");
        Bin binF = saveBin(user, "쓰6", BinType.CIGAR, 126.970632289448, 37.576536739295, "a6");
        Bin binG = saveBin(user, "쓰7", BinType.CIGAR, 126.96968004129, 37.577009971091, "a7");
        Bin binH = saveBin(user, "쓰8", BinType.CIGAR, 126.970393335988, 37.576479015967, "a8");
        Bin binI = saveBin(user, "쓰9", BinType.CIGAR, 126.972894955703, 37.576134445897, "a9");
        Bin binJ = saveBin(user, "쓰10", BinType.CIGAR, 126.969547804414, 37.576762163611, "a10");
        Bin binK = saveBin(user, "쓰11", BinType.CIGAR, 126.969059394981, 37.577133516569, "a11");
        Bin binL = saveBin(user, "쓰12", BinType.CIGAR, 126.971969841012, 37.578567094578, "a12");

        Bookmark bookMark = bookmarkService.createBookMark("dusgh70312@gmail.com", binA.getId());
        Bookmark bookMark1 = bookmarkService.createBookMark("dusgh70312@gmail.com", binB.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binC.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binD.getId());

        bookmarkService.createBookMark("dusgh70312@gmail.com", binE.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binF.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binG.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binH.getId());

        bookmarkService.createBookMark("dusgh70312@gmail.com", binI.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binJ.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binK.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binL.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, bookMark.getId(), 132.131275205);

        assertThat(bookmarks).hasSize(10);

        Bin b2 = binService.findById(binB.getId());
        Bin b3 = binService.findById(binC.getId());
        Bin b4 = binService.findById(binD.getId());
        Bin b5 = binService.findById(binE.getId());
        Bin b6 = binService.findById(binF.getId());
        Bin b7 = binService.findById(binG.getId());
        Bin b8 = binService.findById(binH.getId());
        Bin b9 = binService.findById(binI.getId());
        Bin b10 = binService.findById(binJ.getId());
        Bin b11 = binService.findById(binK.getId());
        Bin b12 = binService.findById(binL.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(
                b2.getId(),
                b3.getId(),
                b4.getId(),
                b5.getId(),
                b6.getId(),
                b7.getId(),
                b8.getId(),
                b9.getId(),
                b10.getId(),
                b11.getId());
    }

    @DisplayName("북마크 조회를 하면 거리순으로 정렬이 돼서 보여진다.")
    @Test
    void bookmark_list_ordered_by_distance(){
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin binA = saveBin(user, "쓰1", BinType.CIGAR, 127.025104317477, 37.496636817721, "a1");
        Bin binE = saveBin(user, "쓰5", BinType.CIGAR, 127.025519919597, 37.493029367378, "a5");
        Bin binD = saveBin(user, "쓰4", BinType.CIGAR, 127.029382413368, 37.498065728468, "a4");
        Bin binB = saveBin(user, "쓰2", BinType.CIGAR, 127.02649140554401, 37.493415595491, "a2");
        Bin binC = saveBin(user, "쓰3", BinType.CIGAR, 127.029123181305, 37.497969565176, "a3");

        bookmarkService.createBookMark("dusgh70312@gmail.com", binA.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binB.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binC.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binD.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binE.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 127.027722755059, 37.4956241314633, null, null);
        assertThat(bookmarks).hasSize(5);
        Bin b1 = binService.findById(binA.getId());
        Bin b2 = binService.findById(binB.getId());
        Bin b3 = binService.findById(binC.getId());
        Bin b4 = binService.findById(binD.getId());
        Bin b5 = binService.findById(binE.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(
                b1.getId(),
                b2.getId(),
                b3.getId(),
                b4.getId(),
                b5.getId());

        assertThat(bookmarks).extracting("address").containsExactly(
                "a1",
                "a2",
                "a3",
                "a4",
                "a5");
        assertThat(bookmarks).extracting("title").containsExactly(
                "쓰1",
                "쓰2",
                "쓰3",
                "쓰4",
                "쓰5");
    }

    @DisplayName("존재하지 않는 회원 아이디로 북마크를 하면 북마크가 되지 않는다.")
    @Test
    void no_member() {

        assertThatThrownBy(() -> bookmarkService.createBookMark("dusgh@gmail.com", bin1.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("이메일과 일치하는 사용자를 찾을 수 없습니다.");
    }

    @DisplayName("존재하지 않는 쓰레기통을 북마크를 하면 북마크가 되지 않는다.")
    @Test
    void no_bin() {

        assertThatThrownBy(() -> bookmarkService.createBookMark("dusgh7031@gmail.com", -23L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 쓰레기통입니다.");
    }

    @DisplayName("삭제된 쓰레기통은 전체 북마크 목록에 포함되지 않는다.")
    @Test
    void deleted_bin_in_all_bookmark() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = saveBin(user, "쓰레기통1", BinType.CIGAR, 126.874538741651, 37.547287215885, "address1");
        Bin bin2 = saveBin(user, "쓰레기통2", BinType.CIGAR, 126.874538741651, 37.547287215885, "address2");

        bookmarkService.createBookMark("user@email.com", bin.getId());
        bookmarkService.createBookMark("user@email.com", bin2.getId());
        adminBinManagementService.deleteBin("admin@email.com", bin.getId(), "그냥");

        List<BookmarkResponse> nearByBookmarks = bookmarkService.getAllBookmarks("user@email.com", 126.874538741651, 37.547287215885, null, null);

        assertThat(nearByBookmarks).hasSize(1);
        assertThat(nearByBookmarks).extracting("address").containsExactly("address2");
        assertThat(nearByBookmarks).extracting("title").contains("쓰레기통2");
        assertThat(nearByBookmarks).extracting("binType").containsExactlyInAnyOrder(BinType.CIGAR);

    }

    @DisplayName("북마크한 쓰레기통이 삭제되면 북마크 숫자도 줄어들어야 한다.")
    @Test
    void bookmark_count_deleted_bin() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        bookmarkService.createBookMark(user.getEmail(), bin1.getId());
        bookmarkService.createBookMark(user.getEmail(), bin2.getId());
        bookmarkService.createBookMark(user.getEmail(), bin3.getId());
        bookmarkService.createBookMark(user.getEmail(), bin4.getId());

        adminBinManagementService.deleteBin("admin@email.com", bin1.getId(), "그냥");
        adminBinManagementService.deleteBin("admin@email.com", bin2.getId(), "그냥");

        Long bookmarkCount = bookmarkRepository.countByMember(user);

        assertThat(bookmarkCount).isEqualTo(2L);

    }


    private Bin saveBin(Member user, String title, BinType binType, Double longitude, Double latitude, String address) {
        Bin bin = new Bin(title, binType, PointUtil.getPoint(longitude, latitude), address, 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        return bin;
    }
}