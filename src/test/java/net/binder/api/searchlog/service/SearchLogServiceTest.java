package net.binder.api.searchlog.service;

import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.bookmark.service.BookmarkService;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.search.dto.SearchResult;
import net.binder.api.search.service.SearchService;
import net.binder.api.searchlog.dto.SearchLogItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SearchLogServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SearchLogServiceTest.class);
    @Autowired
    private SearchLogService searchLogService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private SearchService searchService;

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

    @DisplayName("로그인한 회원이 검색을 하면 키워드, 주소가 검색기록으로 남는다.")
    @Test
    void searchlog(){
        searchService.searchByKeyword(126.971969841012, 37.578567094578,126.971969841012, 37.578567094578, "키워드1", "주소1", "dusgh70312@gmail.com");

        List<SearchLogItem> searchLogItems = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThat(searchLogItems).hasSize(1);
        assertThat(searchLogItems).extracting("keyword").containsExactly("키워드1");
        assertThat(searchLogItems).extracting("address").containsExactly("주소1");
        assertThat(searchLogItems).extracting("hasBookmarkedBin").containsExactly(false);
        assertThat(searchLogItems).extracting("hasBinsNearby").containsExactly(true);

        searchService.searchByKeyword(126.971969841012, 37.578567094578,126.971969841012, 37.578567094578, "키워드1", "주소1", "dusgh70312@gmail.com");

        List<SearchLogItem> searchLogItems2 = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThat(searchLogItems2).hasSize(2);
        assertThat(searchLogItems2).extracting("keyword").containsExactly("키워드1", "키워드1");
        assertThat(searchLogItems2).extracting("address").containsExactly("주소1", "주소1");
        assertThat(searchLogItems2).extracting("hasBookmarkedBin").containsExactly(false, false);
        assertThat(searchLogItems2).extracting("hasBinsNearby").containsExactly(true, true);

    }

    @DisplayName("검색 로그가 없으면 아무것도 나오지 않는다.")
    @Test
    void no_searchlog(){

        List<SearchLogItem> searchLogItems = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThat(searchLogItems).hasSize(0);
    }

    @DisplayName("검색 로그를 삭제할 수 있다.")
    @Test
    void delete_searchlog(){
        searchService.searchByKeyword(126.971969841012, 37.578567094578,126.971969841012, 37.578567094578, "키워드1", "주소1", "dusgh70312@gmail.com");
        searchService.searchByKeyword(126.971969841012, 37.578567094578,126.971969841012, 37.578567094578, "키워드2", "주소2", "dusgh70312@gmail.com");
        List<SearchLogItem> searchLogItems = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        searchLogService.deleteSearchLog("dusgh70312@gmail.com", searchLogItems.get(0).getId());


        List<SearchLogItem> searchLogItems2 = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThat(searchLogItems2).hasSize(1);
        assertThat(searchLogItems2).extracting("keyword").containsExactly("키워드2");
        assertThat(searchLogItems2).extracting("address").containsExactly("주소2");
        assertThat(searchLogItems2).extracting("hasBookmarkedBin").containsExactly( false);
        assertThat(searchLogItems2).extracting("hasBinsNearby").containsExactly(true);

    }

    @DisplayName("검색 결과에 북마크한 쓰레기통이 있으면 북마크 표시가 뜬다.")
    @Test
    void searchlog_with_bookmark(){
        Bookmark bookMark = bookmarkService.createBookMark("dusgh70312@gmail.com", bin1.getId());
        searchService.searchByKeyword(126.971969841012, 37.578567094578,126.971969841012, 37.578567094578, "키워드1", "주소1", "dusgh70312@gmail.com");
        List<SearchResult> searchResults = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드2", "주소2", "dusgh70312@gmail.com");

        List<SearchLogItem> searchLogItems = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThat(searchLogItems).hasSize(2);
        assertThat(searchLogItems).extracting("keyword").containsExactly("키워드1", "키워드2");
        assertThat(searchLogItems).extracting("address").containsExactly("주소1", "주소2");
        assertThat(searchLogItems).extracting("hasBookmarkedBin").containsExactly( true, false);
    }

    @DisplayName("검색 결과가 있으면 검색결과가 있다고 표시된다.")
    @Test
    void searchlog_with_binsnearby(){
        List<SearchResult> searchResults = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드1", "주소1", "dusgh70312@gmail.com");
        List<SearchResult> searchResults1 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드2", "주소2", "dusgh70312@gmail.com");

        assertThat(searchResults.isEmpty()).isFalse();
        assertThat(searchResults1.isEmpty()).isTrue();
        List<SearchLogItem> searchLogItems = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThat(searchLogItems).hasSize(2);
        assertThat(searchLogItems).extracting("keyword").containsExactly("키워드1", "키워드2");
        assertThat(searchLogItems).extracting("address").containsExactly("주소1", "주소2");
        assertThat(searchLogItems).extracting("hasBinsNearby").containsExactly( true, false);
    }

    @DisplayName("검색 결과는 마지막으로 본 검색기록 이후의 10개를 추가로 요청할 수 있다.")
    @Test
    void searchlog_with_pagination(){

        List<SearchResult> searchResults1 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드1", "주소1", "dusgh70312@gmail.com");
        List<SearchResult> searchResults2 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드2", "주소2", "dusgh70312@gmail.com");
        List<SearchResult> searchResults3 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드3", "주소3", "dusgh70312@gmail.com");
        List<SearchResult> searchResults4 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드4", "주소4", "dusgh70312@gmail.com");
        List<SearchResult> searchResults5 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드5", "주소5", "dusgh70312@gmail.com");
        List<SearchResult> searchResults6 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드6", "주소6", "dusgh70312@gmail.com");
        List<SearchResult> searchResults7 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드7", "주소7", "dusgh70312@gmail.com");
        List<SearchResult> searchResults8 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드8", "주소8", "dusgh70312@gmail.com");
        List<SearchResult> searchResults9 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드9", "주소9", "dusgh70312@gmail.com");
        List<SearchResult> searchResults10 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드10", "주소10", "dusgh70312@gmail.com");
        List<SearchResult> searchResults11 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드11", "주소11", "dusgh70312@gmail.com");
        List<SearchResult> searchResults12 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드12", "주소12", "dusgh70312@gmail.com");
        List<SearchResult> searchResults13 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드13", "주소13", "dusgh70312@gmail.com");
        List<SearchResult> searchResults14 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드14", "주소14", "dusgh70312@gmail.com");
        List<SearchResult> searchResults15 = searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드15", "주소15", "dusgh70312@gmail.com");
        List<SearchResult> searchResults16 = searchService.searchByKeyword(126.871969841012, 37.478567094578, 126.871969841012, 37.478567094578, "키워드16", "주소16", "dusgh70312@gmail.com");

        List<SearchLogItem> logs = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThat(logs).hasSize(10);
        assertThat(logs).extracting("keyword").containsExactly(
                "키워드1",
                "키워드2",
                "키워드3",
                "키워드4",
                "키워드5",
                "키워드6",
                "키워드7",
                "키워드8",
                "키워드9",
                "키워드10");
        assertThat(logs).extracting("address").containsExactly(
                "주소1",
                "주소2",
                "주소3",
                "주소4",
                "주소5",
                "주소6",
                "주소7",
                "주소8",
                "주소9",
                "주소10");
        List<SearchLogItem> logs2 = searchLogService.getSearchLog("dusgh70312@gmail.com", logs.get(0).getId());
        assertThat(logs2).hasSize(10);
        assertThat(logs2).extracting("keyword").containsExactly(
                "키워드2",
                "키워드3",
                "키워드4",
                "키워드5",
                "키워드6",
                "키워드7",
                "키워드8",
                "키워드9",
                "키워드10",
                "키워드11");
        assertThat(logs2).extracting("address").containsExactly(
                "주소2",
                "주소3",
                "주소4",
                "주소5",
                "주소6",
                "주소7",
                "주소8",
                "주소9",
                "주소10",
                "주소11");

        List<SearchLogItem> logs3 = searchLogService.getSearchLog("dusgh70312@gmail.com", logs2.get(0).getId());
        assertThat(logs3).hasSize(10);
        assertThat(logs3).extracting("keyword").containsExactly(
                "키워드3",
                "키워드4",
                "키워드5",
                "키워드6",
                "키워드7",
                "키워드8",
                "키워드9",
                "키워드10",
                "키워드11",
                "키워드12");
        assertThat(logs3).extracting("address").containsExactly(
                "주소3",
                "주소4",
                "주소5",
                "주소6",
                "주소7",
                "주소8",
                "주소9",
                "주소10",
                "주소11",
                "주소12");
    }

    @DisplayName("다른 회원의 검색 로그를 지우려고 하면 실패한다.")
    @Test
    void delete_searchlog_fail(){
        searchService.searchByKeyword(126.971969841012, 37.578567094578, 126.971969841012, 37.578567094578, "키워드1", "주소1", "dusgh70312@gmail.com");

        List<SearchLogItem> searchLogItems = searchLogService.getSearchLog("dusgh70312@gmail.com", null);
        assertThatThrownBy(()->searchLogService.deleteSearchLog("dusgh7031@gmail.com", searchLogItems.get(0).getId()))
                .isInstanceOf(BadRequestException.class).hasMessage("해당 회원의 검색 기록이 아닙니다.");
    }

}