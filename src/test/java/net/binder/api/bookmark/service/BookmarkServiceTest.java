package net.binder.api.bookmark.service;

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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;

@Transactional
@SpringBootTest
class BookmarkServiceTest {

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinService binService;

    @Autowired
    private BinRegistrationRepository binRegistrationRepository;

    @Autowired
    private AdminBinRegistrationService adminBinRegistrationService;


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
        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, 300);

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId(), bin3.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1", "address3");
        assertThat(bookmarks).extracting("title").containsExactly("title1", "title3");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR, BinType.BEVERAGE);

        assertThat(bookmarks).extracting("distance")
                .satisfies(distance -> {
                    assertThat((Double) distance.get(0)).isCloseTo(0.0, distanceTolerance);
                    assertThat((Double) distance.get(1)).isEqualTo(132.13127520524972, distanceTolerance);

                });
    }

    @DisplayName("북마크한 것이 없으면 목록 조회를 할 때 아무 것도 뜨지 않는다.")
    @Test
    void no_bookmarks() {

        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, 300);
        assertThat(bookmarks.size()).isEqualTo(0);
    }

    @DisplayName("이미 북마크 한 쓰레기통을 다시 북마크할 수 없다.")
    @Test
    void no_twice_bookmark() {

        bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId());

        assertThatThrownBy(() -> bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 북마크를 한 쓰레기통입니다.");


        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, 300);
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

        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, 300);
        Bin bin = binService.findById(bin1.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(1L);

        bookmarkService.deleteBookMark("dusgh7031@gmail.com", bin1.getId());
        List<BookmarkResponse> bookmarksList = bookmarkService.getNearByBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, 300);
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

        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, 300);
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

        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, 300);
        Bin bin = binService.findById(bin1.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(2L);

        bookmarkService.deleteBookMark("dusgh7031@gmail.com", bin1.getId());
        bookmarkService.deleteBookMark("dusgh70312@gmail.com", bin1.getId());

        List<BookmarkResponse> bookmarks2 = bookmarkService.getAllBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578, null);
        List<BookmarkResponse> bookmarks3 = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, null);

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

        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, 300);
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

    @DisplayName("근처 북마크를 확인하면 가까운 순서대로 정렬된 목록이 조회된다.")
    @Test
    void nearby_bookmark() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        //132.13127520524972
        Bin binA = new Bin("쓰1", BinType.CIGAR, PointUtil.getPoint(126.971968136353, 37.577376610574), "a1", 0L, 0L, 0L, null, null);
        binRepository.save(binA);

        //135.19185992924142
        Bin binB = new Bin("쓰2", BinType.CIGAR, PointUtil.getPoint(126.971100692714, 37.579569691324), "a2", 0L, 0L, 0L, null, null);
        binRepository.save(binB);

        //'170.78071202343938'
        Bin binC = new Bin("쓰3", BinType.CIGAR, PointUtil.getPoint(126.972105810775, 37.57703219307), "a3", 0L, 0L, 0L, null, null);
        binRepository.save(binC);

        //160.2506317060672
        Bin binD = new Bin("쓰4", BinType.CIGAR, PointUtil.getPoint(126.97154998287, 37.579971733838), "a4", 0L, 0L, 0L, null, null);
        binRepository.save(binD);

        BinRegistration binRegistration1 = new BinRegistration(user, binA, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, binB, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, binC, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        BinRegistration binRegistration4 = new BinRegistration(user, binD, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());

        bookmarkService.createBookMark("dusgh70312@gmail.com", binA.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binB.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binC.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binD.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, 300);
        assertThat(bookmarks).hasSize(4);

        assertThat(bookmarks).extracting("address").containsExactly(
                "a1",
                "a2",
                "a4",
                "a3");
        assertThat(bookmarks).extracting("title").containsExactly(
                "쓰1",
                "쓰2",
                "쓰4",
                "쓰3");
        assertThat(bookmarks).extracting("distance")
                .satisfies(distance -> {
                    assertThat((Double) distance.get(0)).isCloseTo(132.13127520524972, distanceTolerance);
                    assertThat((Double) distance.get(1)).isCloseTo(135.19185992924142, distanceTolerance);
                    assertThat((Double) distance.get(2)).isCloseTo(160.2506317060672, distanceTolerance);
                    assertThat((Double) distance.get(3)).isCloseTo(170.78071202343938, distanceTolerance);
                });
    }

    @DisplayName("근처 북마크를 확인하면 최대 5개까지 조회된다.")
    @Test
    void nearby_bookmark_limit_5() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin binA = new Bin("쓰1", BinType.CIGAR, PointUtil.getPoint(126.970393335988, 37.576479015967), "a1", 0L, 0L, 0L, null, null);
        binRepository.save(binA);

        Bin binB = new Bin("쓰2", BinType.CIGAR, PointUtil.getPoint(126.972894955703, 37.576134445897), "a2", 0L, 0L, 0L, null, null);
        binRepository.save(binB);
        Bin binC = new Bin("쓰3", BinType.CIGAR, PointUtil.getPoint(126.969547804414, 37.576762163611), "a3", 0L, 0L, 0L, null, null);
        binRepository.save(binC);


        BinRegistration binRegistration1 = new BinRegistration(user, binA, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, binB, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, binC, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());


        bookmarkService.createBookMark("dusgh70312@gmail.com", bin1.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin2.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin3.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", bin4.getId());

        bookmarkService.createBookMark("dusgh70312@gmail.com", binA.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binB.getId());
        bookmarkService.createBookMark("dusgh70312@gmail.com", binC.getId());


        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, 300);

        assertThat(bookmarks).hasSize(5);
        Bin b = binService.findById(bin1.getId());
        Bin b1 = binService.findById(bin2.getId());
        Bin b2 = binService.findById(bin3.getId());
        Bin b3 = binService.findById(bin4.getId());
        Bin b4 = binService.findById(binA.getId());

        assertThat(bookmarks).extracting("binId").containsExactlyInAnyOrder(
                b.getId(),
                b1.getId(),
                b2.getId(),
                b3.getId(),
                b4.getId());

        assertThat(bookmarks).extracting("address").containsExactlyInAnyOrder(
                "address1",
                "address2",
                "address3",
                "address4",
                "a1");
        assertThat(bookmarks).extracting("title").containsExactlyInAnyOrder(
                "title1",
                "title2",
                "title3",
                "title4",
                "쓰1");
        assertThat(bookmarks).extracting("binType").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.BEVERAGE,
                BinType.BEVERAGE,
                BinType.CIGAR);
        assertThat(b).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b1).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b2).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b3).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b4).extracting("bookmarkCount").isEqualTo(1L);
    }

    @DisplayName("근처 북마크를 확인하면 300M내에 있는 북마크 쓰레기통만 뜬다.")
    @Test
    void nearby_bookmark_by_distance() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));
        Bin binA = new Bin("쓰1", BinType.CIGAR, PointUtil.getPoint(126.970931592059, 37.582100721394), "a1", 0L, 0L, 0L, null, null);
        binRepository.save(binA);


        BinRegistration binRegistration1 = new BinRegistration(user, binA, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());

        bookmarkService.createBookMark("dusgh70312@gmail.com", binA.getId());

        List<BookmarkResponse> bookmarks = bookmarkService.getNearByBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, 300);
        assertThat(bookmarks).hasSize(0);
    }


    @DisplayName("북마크를 리스트를 조회하면 특정 북마크부터 10개씩 받아올 수 있다.(가장 처음 북마크)")
    @Test
    void bookmark_distance_no_offset_by_first_bookmark() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin binA = new Bin("쓰1", BinType.CIGAR, PointUtil.getPoint(126.971100692714, 37.579569691324), "a1", 0L, 0L, 0L, null, null);
        binRepository.save(binA);
        Bin binB = new Bin("쓰2", BinType.CIGAR, PointUtil.getPoint(126.972105810775, 37.57703219307), "a2", 0L, 0L, 0L, null, null);
        binRepository.save(binB);
        Bin binC = new Bin("쓰3", BinType.CIGAR, PointUtil.getPoint(126.97174802679, 37.576573141464), "a3", 0L, 0L, 0L, null, null);
        binRepository.save(binC);
        Bin binD = new Bin("쓰4", BinType.CIGAR, PointUtil.getPoint(126.972759712192, 37.576346238559), "a4", 0L, 0L, 0L, null, null);
        binRepository.save(binD);

        Bin binE = new Bin("쓰5", BinType.CIGAR, PointUtil.getPoint(126.973059283309, 37.582544875368), "a5", 0L, 0L, 0L, null, null);
        binRepository.save(binE);
        Bin binF = new Bin("쓰6", BinType.CIGAR, PointUtil.getPoint(126.974635535622, 37.572958646195), "a6", 0L, 0L, 0L, null, null);
        binRepository.save(binF);
        Bin binG = new Bin("쓰7", BinType.CIGAR, PointUtil.getPoint(126.978983502911, 37.573902932048), "a7", 0L, 0L, 0L, null, null);
        binRepository.save(binG);
        Bin binH = new Bin("쓰8", BinType.CIGAR, PointUtil.getPoint(126.97579707969, 37.570158957488), "a8", 0L, 0L, 0L, null, null);
        binRepository.save(binH);

        BinRegistration binRegistration1 = new BinRegistration(user, binA, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, binB, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, binC, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        BinRegistration binRegistration4 = new BinRegistration(user, binD, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);

        BinRegistration binRegistration5 = new BinRegistration(user, binE, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration5);
        BinRegistration binRegistration6 = new BinRegistration(user, binF, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration6);
        BinRegistration binRegistration7 = new BinRegistration(user, binG, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration7);
        BinRegistration binRegistration8 = new BinRegistration(user, binH, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration8);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration5.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration6.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration7.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration8.getId());

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

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, null);

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
        assertThat(b).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b1).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b2).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b3).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b4).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b5).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b6).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b7).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b8).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b9).extracting("bookmarkCount").isEqualTo(1L);
    }

    @DisplayName("북마크를 리스트를 조회하면 특정 북마크부터 10개씩 받아올 수 있다.")
    @Test
    void bookmark_distance_no_offset() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin binA = new Bin("쓰1", BinType.CIGAR, PointUtil.getPoint(126.971100692714, 37.579569691324), "a1", 0L, 0L, 0L, null, null);
        binRepository.save(binA);
        Bin binB = new Bin("쓰2", BinType.CIGAR, PointUtil.getPoint(126.972105810775, 37.57703219307), "a2", 0L, 0L, 0L, null, null);
        binRepository.save(binB);
        Bin binC = new Bin("쓰3", BinType.CIGAR, PointUtil.getPoint(126.97174802679, 37.576573141464), "a3", 0L, 0L, 0L, null, null);
        binRepository.save(binC);
        Bin binD = new Bin("쓰4", BinType.CIGAR, PointUtil.getPoint(126.972759712192, 37.576346238559), "a4", 0L, 0L, 0L, null, null);
        binRepository.save(binD);

        Bin binE = new Bin("쓰5", BinType.CIGAR, PointUtil.getPoint(126.973059283309, 37.582544875368), "a5", 0L, 0L, 0L, null, null);
        binRepository.save(binE);
        Bin binF = new Bin("쓰6", BinType.CIGAR, PointUtil.getPoint(126.974635535622, 37.572958646195), "a6", 0L, 0L, 0L, null, null);
        binRepository.save(binF);
        Bin binG = new Bin("쓰7", BinType.CIGAR, PointUtil.getPoint(126.978983502911, 37.573902932048), "a7", 0L, 0L, 0L, null, null);
        binRepository.save(binG);
        Bin binH = new Bin("쓰8", BinType.CIGAR, PointUtil.getPoint(126.97579707969, 37.570158957488), "a8", 0L, 0L, 0L, null, null);
        binRepository.save(binH);

        BinRegistration binRegistration1 = new BinRegistration(user, binA, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, binB, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, binC, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        BinRegistration binRegistration4 = new BinRegistration(user, binD, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);

        BinRegistration binRegistration5 = new BinRegistration(user, binE, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration5);
        BinRegistration binRegistration6 = new BinRegistration(user, binF, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration6);
        BinRegistration binRegistration7 = new BinRegistration(user, binG, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration7);
        BinRegistration binRegistration8 = new BinRegistration(user, binH, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration8);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration5.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration6.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration7.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration8.getId());


        Bookmark bookMark = bookmarkService.createBookMark("dusgh70312@gmail.com", bin1.getId());
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

        List<BookmarkResponse> bookmarks = bookmarkService.getAllBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578, bookMark.getId());

        assertThat(bookmarks).hasSize(10);
        Bin b1 = binService.findById(bin2.getId());
        Bin b2 = binService.findById(bin3.getId());
        Bin b3 = binService.findById(bin4.getId());
        Bin b4 = binService.findById(binA.getId());
        Bin b5 = binService.findById(binB.getId());
        Bin b6 = binService.findById(binC.getId());
        Bin b7 = binService.findById(binD.getId());
        Bin b8 = binService.findById(binE.getId());
        Bin b9 = binService.findById(binF.getId());
        Bin b10 = binService.findById(binG.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(
                b1.getId(),
                b2.getId(),
                b3.getId(),
                b4.getId(),
                b5.getId(),
                b6.getId(),
                b7.getId(),
                b8.getId(),
                b9.getId(),
                b10.getId());

        assertThat(bookmarks).extracting("address").containsExactly(
                "address2",
                "address3",
                "address4",
                "a1",
                "a2",
                "a3",
                "a4",
                "a5",
                "a6",
                "a7"
        );
        assertThat(bookmarks).extracting("title").containsExactly(
                "title2",
                "title3",
                "title4",
                "쓰1",
                "쓰2",
                "쓰3",
                "쓰4",
                "쓰5",
                "쓰6",
                "쓰7");
        assertThat(bookmarks).extracting("binType").containsExactly(
                BinType.GENERAL,
                BinType.BEVERAGE,
                BinType.BEVERAGE,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR
        );
        assertThat(b1).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b2).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b3).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b4).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b5).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b6).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b7).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b8).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b9).extracting("bookmarkCount").isEqualTo(1L);
        assertThat(b10).extracting("bookmarkCount").isEqualTo(1L);
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
}