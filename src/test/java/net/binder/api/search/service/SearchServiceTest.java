package net.binder.api.search.service;

import jakarta.persistence.EntityManager;
import net.binder.api.admin.service.AdminBinRegistrationService;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.bookmark.service.BookmarkService;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.binsetup.repository.BinBatchInsertRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import net.binder.api.common.kakaomap.service.KakaoMapService;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.search.dto.SearchResult;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
@SpringBootTest
class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private BinBatchInsertRepository binBatchInsertRepository;

    @Autowired
    private KakaoMapService kakaoMapService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private BinRegistrationRepository binRegistrationRepository;

    @Autowired
    private AdminBinRegistrationService adminBinRegistrationService;

    @Autowired
    private BookmarkService bookMarkService;

    private Member testMember;

    private Offset<Double> xTolerance = within(0.000000000001);

    private Offset<Double> yTolerance = within(0.000000000001);

    @BeforeEach
    void setUp() {
        testMember = new Member("dusgh7031@gmail.com", "테스트", Role.ROLE_USER, "http://example.com/image.jpg");
        memberRepository.save(testMember);
        List<PublicBinData> list = List.of(
                new PublicBinData("서초동 1327-5", "서울 서초구 서초대로78길 24", BinType.CIGAR, null),
                new PublicBinData("던킨도너츠 앞", "서울 서초구 강남대로 373", BinType.RECYCLE, null),
                new PublicBinData("우성아파트I3", "서울 서초구 강남대로 373", BinType.GENERAL, null),
                new PublicBinData("티월드 앞", "서울 서초구 강남대로 359", BinType.RECYCLE, null),
                new PublicBinData("도씨에빛 1 앞", "서울 서초구 강남대로 365", BinType.RECYCLE, null),
                new PublicBinData("강남역2번출구 앞", "서울 강남구 강남대로 382", BinType.GENERAL, null),
                new PublicBinData("강남역2번출구 앞", "서울 강남구 강남대로 382", BinType.RECYCLE, null),
                new PublicBinData("서초동 1330-18", "서울 서초구 서초대로78길 42", BinType.CIGAR, null)
        );
        List<ProcessedBinData> bins = kakaoMapService.getPoints(list);
        binBatchInsertRepository.batchInsertInitialBins(bins);
        entityManager.flush();
    }

    @DisplayName("모든 타입의 쓰레기통을 검색할 수 있다.")
    @Test
    void all_type_searchByCoordinate() {
        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(8);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").contains(
                "서초동 1327-5",
                "우성아파트I3",
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                });
    }

    /*
      타입별 쓰레기통 검색(담배꽁초, 음료수, 일반쓰레기, 재활용쓰레기)
     */

    @DisplayName("담배꽁초 쓰레기통을 검색하면 주변에 있는 담배꽁초 쓰레기통을 찾을 수 있다.")
    @Test
    void cigar_bin_searchByCoordinate() {
        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.49402562647, yTolerance);
                });

        assertThat(search).extracting("distance")
                .satisfies(distanceList -> {
                    assertThat((Double) distanceList.get(0)).isLessThan((Double) distanceList.get(1));
                });
    }

    @DisplayName("음료통 수거함을 검색하면 주변에 있는 음료통 수거함을 찾을 수 있다.")
    @Test
    void beverage_bin_searchByCoordinate() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.BEVERAGE, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address1", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        Bin bin2 = new Bin("쓰레기통123", BinType.BEVERAGE, PointUtil.getPoint(127.02801011993398, 37.495982934664), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        binRegistrationRepository.save(binRegistration2);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());

        List<SearchResult> search = searchService.searchByCoordinate(BinType.BEVERAGE, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly("address1", "address2");
        assertThat(search).extracting("title").containsExactly("쓰레기통12", "쓰레기통123");
        assertThat(search).extracting("type").containsExactly(BinType.BEVERAGE, BinType.BEVERAGE);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027722755059, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.02801011993398, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.4956241314633, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                });
    }

    @DisplayName("음료수 수거함 정보가 없을 때 조회되지 않는다.")
    @Test
    void no_beverage_bin_no_result() {
        List<SearchResult> search = searchService.searchByCoordinate(BinType.BEVERAGE, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");
        assertThat(search.size()).isEqualTo(0);
    }

    @DisplayName("재활용 쓰레기통을 검색하면 재활용 쓰레기통을 찾을 수 있다.")
    @Test
    void search_ByCoordinate_recycle() {
        List<SearchResult> search = searchService.searchByCoordinate(BinType.RECYCLE, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(4);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382"
        );
        assertThat(search).extracting("title").containsExactly(
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞"
        );
        assertThat(search).extracting("type").containsExactly(
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false, false);
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.02862705831201, xTolerance);
                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.49704867762, yTolerance);
                });
    }

    @DisplayName("일반 쓰레기통을 검색하면 주변에 있는 쓰레기통을 찾을 수 있다.")
    @Test
    void search_ByCoordinate_general() {

        List<SearchResult> search = searchService.searchByCoordinate(BinType.GENERAL, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 강남구 강남대로 382");
        assertThat(search).extracting("title").containsExactly(
                "우성아파트I3",
                "강남역2번출구 앞");
        assertThat(search).extracting("type").containsExactly(BinType.GENERAL, BinType.GENERAL);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.02862705831201, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.4970486776201, yTolerance);
                });
    }

    @DisplayName("검색 결과의 첫번째 쓰레기통이 현재 위치에서 가장 가까운 쓰레기통이다.")
    @Test
    void search_ByCoordinate_order_by_distance() {
        List<SearchResult> searchResult = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");
        Double distanceOfFirstBins = searchResult.get(0).getDistance();

        for (int i = 0; i < searchResult.size() - 1; i++) {
            assertThat(searchResult.get(i).getDistance()).isLessThanOrEqualTo(searchResult.get(i + 1).getDistance());
        }

        for (SearchResult result : searchResult) {
            assertThat(distanceOfFirstBins).isLessThanOrEqualTo(result.getDistance());
        }
    }

    @DisplayName("DB에 저장되지 않은 위도, 경도로 조회하면 검색결과가 나오지 않는다.")
    @Test
    void search_ByCoordinate_no_result() {
        //일본의 경도, 위도 좌표 예시
        List<SearchResult> searchResult = searchService.searchByCoordinate(null, 124.11, 35.1, 200, "dusgh7031@gmail.com");
        assertThat(searchResult.size()).isEqualTo(0);
    }

    /*
       쓰레기통 심사와 관련된 로직 (승인/대기/거절)
       타입별로 검색이 제대로 작동하는지 확인하기 위해서 타입별 테스트 진행
     */

    @DisplayName("승인된 쓰레기통은 검색 결과에 포함된다.")
    @Test
    void search_ByCoordinate_result_with_approve() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.GENERAL, PointUtil.getPoint(127.029123181305, 37.497969565176), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration.getId());

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");
        assertThat(search.size()).isEqualTo(9);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42",
                "address"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18",
                "쓰레기통12"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR,
                BinType.GENERAL
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.029123181305, xTolerance);

                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.497969565176, yTolerance);
                });
    }

    @DisplayName("승인된 쓰레기통은 검색 결과에 포함돼야 한다(일반 쓰레기)")
    @Test
    void search_ByCoordinate_general_bin_with_approve() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.GENERAL, PointUtil.getPoint(127.029123181305, 37.497969565176), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration.getId());

        List<SearchResult> search = searchService.searchByCoordinate(BinType.GENERAL, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(3);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 강남구 강남대로 382",
                "address");
        assertThat(search).extracting("title").containsExactly(
                "우성아파트I3",
                "강남역2번출구 앞",
                "쓰레기통12");
        assertThat(search).extracting("type").containsExactly(BinType.GENERAL, BinType.GENERAL, BinType.GENERAL);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false);
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.02862705831201, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.029123181305, xTolerance);
                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.4970486776201, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.497969565176, yTolerance);
                });
    }

    @DisplayName("승인된 쓰레기통은 검색 결과에 포함돼야 한다(담배꽁초)")
    @Test
    void search_ByCoordinate_cigar_bin_with_approve() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.025104317477, 37.496636817721), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration.getId());

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(3);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 서초대로78길 42",
                "address");
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "서초동 1330-18",
                "쓰레기통12");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.025104317477, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.496636817721, yTolerance);
                });
    }

    /*
       승인 대기 상황 테스트
     */

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.")
    @Test
    void search_ByCoordinate_result_without_pending() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.02862705831201, 37.49704867762), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");
        assertThat(search.size()).isEqualTo(8);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                });
    }

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(담배꽁초)")
    @Test
    void search_ByCoordinate_cigar_bin_without_pending() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.02862705831201, 37.49704867762), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);

        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.49402562647, yTolerance);
                });

        assertThat(search).extracting("distance")
                .satisfies(distanceList -> {
                    assertThat((Double) distanceList.get(0)).isLessThan((Double) distanceList.get(1));
                });

    }

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(음료수)")
    @Test
    void search_ByCoordinate_beverage_bin_without_pending() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.BEVERAGE, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        List<SearchResult> search = searchService.searchByCoordinate(BinType.BEVERAGE, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(0);
    }

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(재활용)")
    @Test
    void search_ByCoordinate_recycle_bin_without_pending() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.RECYCLE, PointUtil.getPoint(127.02801011993398, 37.495982934664), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        List<SearchResult> search = searchService.searchByCoordinate(BinType.RECYCLE, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(4);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382"
        );
        assertThat(search).extracting("title").containsExactly(
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞"
        );
        assertThat(search).extracting("type").containsExactly(
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false, false);
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.02862705831201, xTolerance);
                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.49704867762, yTolerance);
                });

    }

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(일반)")
    @Test
    void search_ByCoordinate_general_bin_without_pending() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.GENERAL, PointUtil.getPoint(127.02801011993398, 37.495982934664), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        List<SearchResult> search = searchService.searchByCoordinate(BinType.GENERAL, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 강남구 강남대로 382");
        assertThat(search).extracting("title").containsExactly(
                "우성아파트I3",
                "강남역2번출구 앞");
        assertThat(search).extracting("type").containsExactly(BinType.GENERAL, BinType.GENERAL);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.02862705831201, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.4970486776201, yTolerance);
                });
    }

    /*
       거절된 쓰레기통
     */

    @DisplayName("승인 거절된 쓰레기통은 검색 결과에서 제외돼야 한다.")
    @Test
    void search_ByCoordinate_bins_without_reject() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));
        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.02862705831201, 37.49704867762), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(8);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                });
    }

    @DisplayName("승인을 거절한 쓰레기통은 검색 결과에서 제외돼야 한다.(담배꽁초)")
    @Test
    void search_ByCoordinate_cigar_bins_without_reject() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.02862705831201, 37.49704867762), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isCloseTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isCloseTo(37.49402562647, yTolerance);
                });

        assertThat(search).extracting("distance")
                .satisfies(distanceList -> assertThat((Double) distanceList.get(0)).isLessThan((Double) distanceList.get(1)));

    }

    @DisplayName("거절된 쓰레기통은 검색 결과에서 제외돼야 한다.(음료수)")
    @Test
    void search_ByCoordinate_beverage_bin_without_reject() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.BEVERAGE, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        List<SearchResult> search = searchService.searchByCoordinate(BinType.BEVERAGE, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(0);
    }

    @DisplayName("거절된 쓰레기통은 검색 결과에서 제외돼야 한다.(재활용)")
    @Test
    void search_ByCoordinate_recycle_bin_without_reject() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.RECYCLE, PointUtil.getPoint(127.02801011993398, 37.495982934664), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        List<SearchResult> search = searchService.searchByCoordinate(BinType.RECYCLE, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(4);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382"
        );
        assertThat(search).extracting("title").containsExactly(
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞"
        );
        assertThat(search).extracting("type").containsExactly(
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false, false);
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.02862705831201, xTolerance);
                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.49704867762, yTolerance);
                });

    }

    @DisplayName("거절된 쓰레기통은 검색 결과에서 제외돼야 한다.(일반)")
    @Test
    void search_ByCoordinate_general_bin_without_reject() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.GENERAL, PointUtil.getPoint(127.02801011993398, 37.495982934664), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        List<SearchResult> search = searchService.searchByCoordinate(BinType.GENERAL, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 강남구 강남대로 382");
        assertThat(search).extracting("title").containsExactly(
                "우성아파트I3",
                "강남역2번출구 앞");
        assertThat(search).extracting("type").containsExactly(BinType.GENERAL, BinType.GENERAL);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.02862705831201, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.4970486776201, yTolerance);
                });
    }

   /*
    비즈니스 로직 테스트
    */

    @DisplayName("반경 제한을 넘으면, 최대 반경으로 검색된다.")
    @Test
    void test_over_max_radius() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.028754000454, 37.498681360529), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통123", BinType.BEVERAGE, PointUtil.getPoint(127.026692446306, 37.498775008377), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);
        Bin bin3 = new Bin("500M이상1", BinType.GENERAL, PointUtil.getPoint(127.031703595662, 37.498784671997), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);
        Bin bin4 = new Bin("500M이상2", BinType.RECYCLE, PointUtil.getPoint(127.031062762603, 37.499416177304), "address4", 0L, 0L, 0L, null, null);
        binRepository.save(bin4);

        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, bin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        BinRegistration binRegistration4 = new BinRegistration(user, bin4, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 1000, "dusgh7031@gmail.com");
        assertThat(search.size()).isEqualTo(10);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42",
                "address",
                "address2"

        );
        assertThat(search).extracting("title").contains(
                "서초동 1327-5",
                "우성아파트I3",
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18",
                "쓰레기통12",
                "쓰레기통123"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.BEVERAGE
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.028754000454, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.026692446306, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.498681360529, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.498775008377, yTolerance);
                });


        assertThat(search).extracting("distance")
                .satisfies(distanceList -> {
                    assertThat((Double) distanceList.get(0)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(1)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(2)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(3)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(4)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(5)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(6)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(7)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(8)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(9)).isLessThanOrEqualTo(500.0);
                });
    }

    @DisplayName("반경 제한을 넘으면, 최대 반경으로 검색된다.(타입검색)")
    @Test
    void test_over_max_radius_with_cigar_bins() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.025104317477, 37.496636817721), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통123", BinType.CIGAR, PointUtil.getPoint(127.027400362129, 37.499975707358), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);
        Bin bin3 = new Bin("500M이상1", BinType.CIGAR, PointUtil.getPoint(127.025700266315, 37.505196066016), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);
        Bin bin4 = new Bin("500M이상2", BinType.CIGAR, PointUtil.getPoint(127.025700266315, 37.505196066016), "address4", 0L, 0L, 0L, null, null);
        binRepository.save(bin4);

        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, bin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        BinRegistration binRegistration4 = new BinRegistration(user, bin4, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 1000, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(4);

        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 서초대로78길 42",
                "address",
                "address2");
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "서초동 1330-18",
                "쓰레기통12",
                "쓰레기통123");
        assertThat(search).extracting("type").containsExactly(
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.025104317477, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.027400362129, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.496636817721, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.499975707358, yTolerance);
                });

        assertThat(search).extracting("distance")
                .satisfies(distanceList -> {
                    assertThat((Double) distanceList.get(0)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(1)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(2)).isLessThanOrEqualTo(500.0);
                    assertThat((Double) distanceList.get(3)).isLessThanOrEqualTo(500.0);
                });
    }

    @DisplayName("반경이 최소 제한을 넘지 않으면 100m로 검색된다.")
    @Test
    void test_over_minimum_radius() {

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 10, "dusgh7031@gmail.com");
        assertThat(search.size()).isEqualTo(4);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365"
        );
        assertThat(search).extracting("title").contains(
                "서초동 1327-5",
                "우성아파트I3",
                "던킨도너츠 앞",
                "도씨에빛 1 앞"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false
        );

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);

                });
    }

    @DisplayName("반경이 최소 제한을 넘지 않으면 100m로 검색된다.(타입검색)")
    @Test
    void test_over_minimum_radius_with_cigar_bins() {

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 10, "dusgh7031@gmail.com");
        assertThat(search.size()).isEqualTo(1);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24"
        );
        assertThat(search).extracting("title").contains(
                "서초동 1327-5"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false
        );

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                });
    }

    @DisplayName("검색 반경을 200m로 제한하면, 200m내에 있는 쓰레기통만 조회된다")
    @Test
    void search_ByCoordinate_within_200M() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(8);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").contains(
                "서초동 1327-5",
                "우성아파트I3",
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                });

        assertThat(search).extracting("distance")
                .satisfies(distanceList -> {
                    assertThat((Double) distanceList.get(0)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(1)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(2)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(3)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(4)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(5)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(6)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(7)).isLessThanOrEqualTo(200.0);
                });
    }

    @DisplayName("검색 반경을 200m로 제한하면, 200m내에 있는 쓰레기통만 조회된다(타입검색)")
    @Test
    void search_ByCoordinate_bins_cigar_within_200M() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.025104317477, 37.496636817721), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isCloseTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isCloseTo(37.49402562647, yTolerance);
                });

        assertThat(search).extracting("distance")
                .satisfies(distanceList -> {
                    assertThat((Double) distanceList.get(0)).isLessThanOrEqualTo(200.0);
                    assertThat((Double) distanceList.get(1)).isLessThanOrEqualTo(200.0);
                });
    }

    @DisplayName("검색결과는 최대 10개까지만 반환된다.")
    @Test
    void search_ByCoordinate_size_ten() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.02649140554401, 37.493415595491), "address1", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통2", BinType.CIGAR, PointUtil.getPoint(127.029241204878, 37.493519553805), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);
        Bin bin3 = new Bin("쓰레기통3", BinType.CIGAR, PointUtil.getPoint(127.029123181305, 37.497969565176), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);
        Bin bin4 = new Bin("쓰레기통4", BinType.CIGAR, PointUtil.getPoint(127.029123181305, 37.497969565176), "address4", 0L, 0L, 0L, null, null);
        binRepository.save(bin4);

        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, bin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        BinRegistration binRegistration4 = new BinRegistration(user, bin4, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 500, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(10);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42",
                "address1",
                "address2"

        );
        assertThat(search).extracting("title").contains(
                "서초동 1327-5",
                "우성아파트I3",
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18",
                "쓰레기통1",
                "쓰레기통2"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR
        );

        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.02649140554401, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.029241204878, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.493415595491, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.493519553805, yTolerance);
                });

    }

    @DisplayName("검색결과는 최대 10개까지 반환된다.(타입검색)")
    @Test
    void search_ByCoordinate_cigar_bins_size_ten() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.029382413368, 37.498065728468), "address1", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통2", BinType.CIGAR, PointUtil.getPoint(127.029382413368, 37.498065728468), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);
        Bin bin3 = new Bin("쓰레기통3", BinType.CIGAR, PointUtil.getPoint(127.025519919597, 37.493029367378), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);
        Bin bin4 = new Bin("쓰레기통4", BinType.CIGAR, PointUtil.getPoint(127.028754000454, 37.498681360529), "address4", 0L, 0L, 0L, null, null);
        binRepository.save(bin4);
        Bin bin5 = new Bin("쓰레기통5", BinType.CIGAR, PointUtil.getPoint(127.028754000454, 37.498681360529), "address5", 0L, 0L, 0L, null, null);
        binRepository.save(bin5);
        Bin bin6 = new Bin("쓰레기통6", BinType.CIGAR, PointUtil.getPoint(127.026692446306, 37.498775008377), "address6", 0L, 0L, 0L, null, null);
        binRepository.save(bin6);
        Bin bin7 = new Bin("쓰레기통7", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address7", 0L, 0L, 0L, null, null);
        binRepository.save(bin7);
        Bin bin8 = new Bin("쓰레기통8", BinType.CIGAR, PointUtil.getPoint(127.02754201132602, 37.499218198539), "address8", 0L, 0L, 0L, null, null);
        binRepository.save(bin8);
        Bin bin9 = new Bin("쓰레기통9", BinType.CIGAR, PointUtil.getPoint(127.02754201132602, 37.499218198539), "address9", 0L, 0L, 0L, null, null);
        binRepository.save(bin9);
        Bin bin10 = new Bin("쓰레기통10", BinType.CIGAR, PointUtil.getPoint(127.02953329109899, 37.498978139947), "address10", 0L, 0L, 0L, null, null);
        binRepository.save(bin10);

        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, bin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        BinRegistration binRegistration4 = new BinRegistration(user, bin4, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);

        BinRegistration binRegistration5 = new BinRegistration(user, bin5, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration5);
        BinRegistration binRegistration6 = new BinRegistration(user, bin6, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration6);
        BinRegistration binRegistration7 = new BinRegistration(user, bin7, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration7);
        BinRegistration binRegistration8 = new BinRegistration(user, bin8, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration8);

        BinRegistration binRegistration9 = new BinRegistration(user, bin9, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration9);
        BinRegistration binRegistration10 = new BinRegistration(user, bin10, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration10);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration5.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration6.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration7.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration8.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration9.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration10.getId());

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 5000, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(10);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 서초대로78길 42",
                "address1",
                "address2",
                "address3",
                "address4",
                "address5",
                "address6",
                "address7",
                "address8"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "서초동 1330-18",
                "쓰레기통1",
                "쓰레기통2",
                "쓰레기통3",
                "쓰레기통4",
                "쓰레기통5",
                "쓰레기통6",
                "쓰레기통7",
                "쓰레기통8");
        assertThat(search).extracting("type").containsExactly(
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.029382413368, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.029382413368, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.025519919597, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028754000454, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028754000454, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.026692446306, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.029588346617, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.02754201132602, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.498065728468, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.498065728468, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.493029367378, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.498681360529, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.498681360529, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.498775008377, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.492625668276, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.499218198539, yTolerance);
                });
    }

    @DisplayName("비로그인 이용자도 쓰레기통 검색을 할 수 있다.")
    @Test
    void search_ByCoordinate_for_no_login_user() {

        List<SearchResult> search = searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, null);
        assertThat(search.size()).isEqualTo(2);
        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.49402562647, yTolerance);
                });
    }

    @DisplayName("로그인한 유저에게는 즐겨찾기를 누른 쓰레기통이 표시된다.")
    @Test
    void search_ByCoordinate_result_bookmark() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.029382413368, 37.498065728468), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin = binRepository.save(bin);

        Bin bin2 = new Bin("쓰레기통2", BinType.CIGAR, PointUtil.getPoint(127.028754000454, 37.498681360529), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin2 = binRepository.save(bin2);

        BinRegistration binRegistration1 = new BinRegistration(user, savedBin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, savedBin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());

        bookMarkService.createBookMark(user.getEmail(), savedBin.getId());
        bookMarkService.createBookMark(user.getEmail(), savedBin2.getId());

        List<SearchResult> search = searchService.searchByCoordinate(null, 127.027722755059, 37.4956241314633, 500, "user@email.com");

        assertThat(search.size()).isEqualTo(10);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42",
                "address1",
                "address2"
        );
        assertThat(search).extracting("title").contains(
                "서초동 1327-5",
                "우성아파트I3",
                "던킨도너츠 앞",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18",
                "쓰레기통1",
                "쓰레기통2"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.CIGAR,
                BinType.CIGAR,
                BinType.CIGAR
        );
        assertThat(search).extracting("isBookMarked").containsExactly(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                true,
                true
        );
        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.029382413368, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.028754000454, xTolerance);
                });
        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.498065728468, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.498681360529, yTolerance);
                });
    }

     /*
     입력값 테스트
     한국의 위도는 124도에서 132도, 경도는 33~ 43도
     */

    @DisplayName("한국의 경도를 벗어나면 검색이 실패한다.(1) ")
    @Test
    void test_with_wrong_latitude() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 124.5555, 32.99999999999, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 위도를 벗어나면 검색이 실패한다.(2) ")
    @Test
    void test_with_wrong_longitude() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 123.9999999999999, 33.3332, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(3)")
    @Test
    void test_with_wrong_latitude_2() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 132.65, 44.000001, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 위도를 벗어나면 검색이 실패한다.(4) ")
    @Test
    void test_with_wrong_longitude_2() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 133.002211, 34.5, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(5) ")
    @Test
    void test_with_wrong_latitude_3() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 125.2, 0.1, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 위도를 벗어나면 검색이 실패한다.(6) ")
    @Test
    void test_with_wrong_longitude_3() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 0.1, 35.111, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(7) ")
    @Test
    void test_with_wrong_latitude_4() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 125.2, -1.2, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 위도를 벗어나면 검색이 실패한다.(8) ")
    @Test
    void test_with_wrong_longitude_4() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, -2.3, 35.111, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("반경을 음수로 넣으면 결과가 반환되지 않는다.")
    @Test
    void test_with_wrong_radius() {
        assertThatThrownBy(() ->
                searchService.searchByCoordinate(BinType.CIGAR, 127.027722755059, 37.495544565616, -23, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 반경 설정입니다.");
    }


    //키워드 검색 테스트

    @DisplayName("키워드로 검색하면 검색 결과가 나온다.")
    @Test
    void search_by_keyword(){

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin = binRepository.save(bin);

        BinRegistration binRegistration1 = new BinRegistration(user, savedBin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());

        List<SearchResult> searchResults = searchService.searchByKeyword(127.027752353367, 37.495544565616, 127.031360322259, 37.489194715316, "키워드1", "주소1", null);

        assertThat(searchResults).hasSize(1);
        assertThat(searchResults).extracting("address").containsExactly("address1");
        assertThat(searchResults).extracting("title").contains("쓰레기통1");
        assertThat(searchResults).extracting("type").containsExactlyInAnyOrder(BinType.CIGAR);
        assertThat(searchResults).extracting("isBookMarked").containsExactly(false);
    }

    @DisplayName("키워드로 검색하면 검색 결과에는 현재 위치에서 얼마나 떨어져있는지 거리가 계산된다.")
    @Test
    void search_by_keyword_with_distance(){

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin = binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통2", BinType.CIGAR, PointUtil.getPoint(127.03034053027, 37.491751140026), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin2 = binRepository.save(bin2);
        Bin bin3 = new Bin("쓰레기통3", BinType.CIGAR, PointUtil.getPoint(127.030921234166, 37.492427285546), "address3", 0L, 0L, 0L, null, null);
        Bin savedBin3 = binRepository.save(bin3);


        BinRegistration binRegistration1 = new BinRegistration(user, savedBin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());

        BinRegistration binRegistration2 = new BinRegistration(user, savedBin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());

        BinRegistration binRegistration3 = new BinRegistration(user, savedBin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());

        List<SearchResult> searchResults = searchService.searchByKeyword(127.027752353367, 37.495544565616, 127.031360322259, 37.489194715316, "키워드1", "주소1", null);

        assertThat(searchResults).hasSize(3);
        assertThat(searchResults).extracting("address").containsExactly("address1", "address3", "address2");
        assertThat(searchResults).extracting("title").contains("쓰레기통1","쓰레기통3", "쓰레기통2");
        assertThat(searchResults).extracting("type").containsExactlyInAnyOrder(BinType.CIGAR, BinType.CIGAR, BinType.CIGAR);
        assertThat(searchResults).extracting("isBookMarked").containsExactly(false, false, false);

        assertThat(searchResults).extracting(SearchResult::getDistance)
                .satisfies(distance -> {
                    assertThat(distance.get(0)).isCloseTo(362.3704015636515, within(0.01));
                    assertThat(distance.get(1)).isCloseTo(445.23196905568466, within(0.01));
                    assertThat(distance.get(2)).isCloseTo(479.21466852706027, within(0.01));
                });
    }

    @DisplayName("키워드로 검색하면 검색 결과는 10개까지만 나온다.")
    @Test
    void search_by_keyword_limit(){

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin = binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통2", BinType.CIGAR, PointUtil.getPoint(127.03034053027, 37.491751140026), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin2 = binRepository.save(bin2);
        Bin bin3 = new Bin("쓰레기통3", BinType.CIGAR, PointUtil.getPoint(127.030921234166, 37.492427285546), "address3", 0L, 0L, 0L, null, null);
        Bin savedBin3 = binRepository.save(bin3);

        Bin bin4 = new Bin("쓰레기통4", BinType.BEVERAGE, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin4 = binRepository.save(bin4);
        Bin bin5 = new Bin("쓰레기통5", BinType.BEVERAGE, PointUtil.getPoint(127.03034053027, 37.491751140026), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin5 = binRepository.save(bin5);
        Bin bin6 = new Bin("쓰레기통6", BinType.BEVERAGE, PointUtil.getPoint(127.030921234166, 37.492427285546), "address3", 0L, 0L, 0L, null, null);
        Bin savedBin6 = binRepository.save(bin6);

        Bin bin7 = new Bin("쓰레기통7", BinType.GENERAL, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin7 = binRepository.save(bin7);
        Bin bin8 = new Bin("쓰레기통8", BinType.GENERAL, PointUtil.getPoint(127.03034053027, 37.491751140026), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin8 = binRepository.save(bin8);
        Bin bin9 = new Bin("쓰레기통9", BinType.GENERAL, PointUtil.getPoint(127.030921234166, 37.492427285546), "address3", 0L, 0L, 0L, null, null);
        Bin savedBin9 = binRepository.save(bin9);

        Bin bin10 = new Bin("쓰레기통10", BinType.RECYCLE, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin10 = binRepository.save(bin10);
        Bin bin11 = new Bin("쓰레기통11", BinType.RECYCLE, PointUtil.getPoint(127.03034053027, 37.491751140026), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin11 = binRepository.save(bin11);
        Bin bin12 = new Bin("쓰레기통12", BinType.RECYCLE, PointUtil.getPoint(127.030921234166, 37.492427285546), "address3", 0L, 0L, 0L, null, null);
        Bin savedBin12 = binRepository.save(bin12);


        BinRegistration binRegistration1 = new BinRegistration(user, savedBin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        BinRegistration binRegistration2 = new BinRegistration(user, savedBin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        BinRegistration binRegistration3 = new BinRegistration(user, savedBin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());

        BinRegistration binRegistration4 = new BinRegistration(user, savedBin4, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration4);
        BinRegistration binRegistration5 = new BinRegistration(user, savedBin5, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration5);
        BinRegistration binRegistration6 = new BinRegistration(user, savedBin6, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration6);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration4.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration5.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration6.getId());

        BinRegistration binRegistration7 = new BinRegistration(user, savedBin7, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration7);
        BinRegistration binRegistration8 = new BinRegistration(user, savedBin8, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration8);
        BinRegistration binRegistration9 = new BinRegistration(user, savedBin9, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration9);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration7.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration8.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration9.getId());


        BinRegistration binRegistration10 = new BinRegistration(user, savedBin10, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration10);
        BinRegistration binRegistration11 = new BinRegistration(user, savedBin11, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration11);
        BinRegistration binRegistration12 = new BinRegistration(user, savedBin12, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration12);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration10.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration11.getId());
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration12.getId());

        List<SearchResult> searchResults = searchService.searchByKeyword(127.027752353367, 37.495544565616, 127.031360322259, 37.489194715316, "키워드1", "주소1", null);

        assertThat(searchResults).hasSize(10);
    }

    @DisplayName("키워드로 검색하면 좋아요를 누른 쓰레기통이 표시된다.")
    @Test
    void search_by_keyword_with_bookmark(){

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin = binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통2", BinType.CIGAR, PointUtil.getPoint(127.03034053027, 37.491751140026), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin2 = binRepository.save(bin2);
        Bin bin3 = new Bin("쓰레기통3", BinType.CIGAR, PointUtil.getPoint(127.030921234166, 37.492427285546), "address3", 0L, 0L, 0L, null, null);
        Bin savedBin3 = binRepository.save(bin3);


        BinRegistration binRegistration1 = new BinRegistration(user, savedBin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());


        BinRegistration binRegistration2 = new BinRegistration(user, savedBin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());

        BinRegistration binRegistration3 = new BinRegistration(user, savedBin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());

        bookMarkService.createBookMark("user@email.com", bin.getId());

        List<SearchResult> searchResults = searchService.searchByKeyword(127.027752353367, 37.495544565616, 127.031360322259, 37.489194715316, "키워드1", "주소1", "user@email.com");

        assertThat(searchResults).hasSize(3);
        assertThat(searchResults).extracting("address").containsExactly("address1", "address3", "address2");
        assertThat(searchResults).extracting("title").contains("쓰레기통1","쓰레기통3", "쓰레기통2");
        assertThat(searchResults).extracting("type").containsExactlyInAnyOrder(BinType.CIGAR, BinType.CIGAR, BinType.CIGAR);
        assertThat(searchResults).extracting("isBookMarked").containsExactly(true, false, false);

        assertThat(searchResults).extracting(SearchResult::getDistance)
                .satisfies(distance -> {
                    assertThat(distance.get(0)).isCloseTo(362.3704015636515, within(0.01));
                    assertThat(distance.get(1)).isCloseTo(445.23196905568466, within(0.01));
                    assertThat(distance.get(2)).isCloseTo(479.21466852706027, within(0.01));
                });
    }

    @DisplayName("키워드 검색시 검색 결과는 500m 반경에 있는 것들로만 나온다.")
    @Test
    void search_by_keyword_with_distance_limit(){

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통1", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address1", 0L, 0L, 0L, null, null);
        Bin savedBin = binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통2", BinType.CIGAR, PointUtil.getPoint(127.032367803374, 37.49993264406), "address2", 0L, 0L, 0L, null, null);
        Bin savedBin2 = binRepository.save(bin2);
        Bin bin3 = new Bin("쓰레기통3", BinType.CIGAR, PointUtil.getPoint(127.030921234166, 37.492427285546), "address3", 0L, 0L, 0L, null, null);
        Bin savedBin3 = binRepository.save(bin3);


        BinRegistration binRegistration1 = new BinRegistration(user, savedBin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());


        BinRegistration binRegistration2 = new BinRegistration(user, savedBin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration2.getId());

        BinRegistration binRegistration3 = new BinRegistration(user, savedBin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration3.getId());

        bookMarkService.createBookMark("user@email.com", bin.getId());

        List<SearchResult> searchResults = searchService.searchByKeyword(127.027752353367, 37.495544565616, 127.031360322259, 37.489194715316, "키워드1", "주소1", null);

        assertThat(searchResults).hasSize(2);
        assertThat(searchResults).extracting("address").containsExactly("address1", "address3");
        assertThat(searchResults).extracting("title").contains("쓰레기통1","쓰레기통3");
        assertThat(searchResults).extracting("type").containsExactlyInAnyOrder(BinType.CIGAR, BinType.CIGAR);
        assertThat(searchResults).extracting("isBookMarked").containsExactly(false, false);

        assertThat(searchResults).extracting(SearchResult::getDistance)
                .satisfies(distance -> {
                    assertThat(distance.get(0)).isCloseTo(362.3704015636515, within(0.01));
                    assertThat(distance.get(1)).isCloseTo(445.23196905568466, within(0.01));
                });
    }

}