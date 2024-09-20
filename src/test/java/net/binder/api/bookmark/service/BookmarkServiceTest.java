package net.binder.api.bookmark.service;

import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.service.BinService;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.bookmark.repository.BookmarkRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

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
    private BookmarkRepository bookmarkRepository;

    private Member testMember;

    private Member testMember2;

    private Bin bin1;

    private Bin bin2;

    private Bin bin3;

    private Bin bin4;

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
        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId(), bin3.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1", "address3");
        assertThat(bookmarks).extracting("title").containsExactly("title1", "title3");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR, BinType.BEVERAGE);

        assertThat(bookmarks).extracting("distance")
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(0.0);
                    assertThat(yList.get(1)).isEqualTo(132.13127520524972);

                });
    }

    @DisplayName("북마크한 것이 없으면 목록 조회를 할 때 아무 것도 뜨지 않는다.")
    @Test
    void no_bookmarks() {

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);
        assertThat(bookmarks.size()).isEqualTo(0);
    }

    @DisplayName("이미 북마크 한 쓰레기통을 다시 북마크할 수 없다.")
    @Test
    void no_twice_bookmark() {

        bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId());

        assertThatThrownBy(() -> bookmarkService.createBookMark("dusgh7031@gmail.com", bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 북마크를 한 쓰레기통입니다.");


        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);
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

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);
        Bin bin = binService.findById(bin1.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(1L);

        bookmarkService.deleteBookMark("dusgh7031@gmail.com", bin1.getId());
        List<BookmarkResponse> bookmarksList = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);
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

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);
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

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);
        Bin bin = binService.findById(bin1.getId());

        assertThat(bookmarks).extracting("binId").containsExactly(bin1.getId());
        assertThat(bookmarks).extracting("address").containsExactly("address1");
        assertThat(bookmarks).extracting("title").containsExactly("title1");
        assertThat(bookmarks).extracting("binType").containsExactly(BinType.CIGAR);
        assertThat(bin).extracting("bookmarkCount").isEqualTo(2L);

        bookmarkService.deleteBookMark("dusgh7031@gmail.com", bin1.getId());
        bookmarkService.deleteBookMark("dusgh70312@gmail.com", bin1.getId());

        List<BookmarkResponse> bookmarks2 = bookmarkService.getBookmarks("dusgh7031@gmail.com", 126.971969841012, 37.578567094578);
        List<BookmarkResponse> bookmarks3 = bookmarkService.getBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578);

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

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarks("dusgh70312@gmail.com", 126.971969841012, 37.578567094578);
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
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(0.0);
                    assertThat(yList.get(1)).isEqualTo(132.13127520524972);
                    assertThat(yList.get(2)).isEqualTo(132.13127520524972);
                    assertThat(yList.get(3)).isEqualTo(160.2506317060672);

                });
    }

    @DisplayName("존재하지 않는 회원 아이디로 북마크를 하면 북마크가 되지 않는다..")
    @Test
    void no_member() {

        assertThatThrownBy(() -> bookmarkService.createBookMark("dusgh@gmail.com", bin1.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("이메일과 일치하는 사용자를 찾을 수 없습니다.");
    }

    @DisplayName("존재하지 않는 쓰레기통을 북마크를 하면 북마크가 되지 않는다..")
    @Test
    void no_bin() {

        assertThatThrownBy(() -> bookmarkService.createBookMark("dusgh7031@gmail.com", -23L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 쓰레기통입니다.");
    }

}