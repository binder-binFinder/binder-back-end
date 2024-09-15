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
import static org.junit.jupiter.api.Assertions.*;

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
    void all_type_search() {
        List<SearchResult> search = searchService.search(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

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
    void cigar_bin_search() {
        List<SearchResult> search = searchService.search(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

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
    void beverage_bin_search() {

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

        List<SearchResult> search = searchService.search(BinType.BEVERAGE, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

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
        List<SearchResult> search = searchService.search(BinType.BEVERAGE, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");
        assertThat(search.size()).isEqualTo(0);
    }

    @DisplayName("재활용 쓰레기통을 검색하면 재활용 쓰레기통을 찾을 수 있다.")
    @Test
    void 재활용_쓰레기통_검색() {
        List<SearchResult> search = searchService.search(BinType.RECYCLE, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(5);
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
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false, false, false);

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
    void 일반_쓰레기통_검색() {

        List<SearchResult> search = searchService.search(BinType.GENERAL, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(4);

        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 382");
        assertThat(search).extracting("title").containsExactly(
                "우성아파트O3",
                "우성아파트I3",
                "강남대륭빌딩 앞",
                "강남역2번출구 앞");
        assertThat(search).extracting("type").containsExactly(BinType.GENERAL, BinType.GENERAL, BinType.GENERAL, BinType.GENERAL);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.02862705831201, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.49704867762, yTolerance);
                });
    }

    @DisplayName("검색 결과의 첫번째 쓰레기통이 현재 위치에서 가장 가까운 쓰레기통이다.")
    @Test
    void 쓰레기통_거리() {
        List<SearchResult> searchResult = searchService.search(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");
        Double distanceOfFirstBins = searchResult.get(0).getDistance();

        for (int i = 0; i < searchResult.size() - 1; i++) {
            assertThat(searchResult.get(i).getDistance()).isLessThanOrEqualTo(searchResult.get(i + 1).getDistance());
        }

        for (int i = 0; i < searchResult.size(); i++) {
            assertThat(distanceOfFirstBins).isLessThanOrEqualTo(searchResult.get(i).getDistance());
        }
    }

    @DisplayName("비로그인 이용자도 쓰레기통 검색을 할 수 있다.")
    @Test
    void 비로그인_쓰레기통_검색() {

        List<SearchResult> search = searchService.search(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, null);

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

    @DisplayName("DB에 저장되지 않은 위도, 경도로 조회하면 검색결과가 나오지 않는다.")
    @Test
    void DB_데이터_좌표_기준_검색() {

        //일본의 경도, 위도 좌표 예시
        List<SearchResult> searchResult = searchService.search(null, 124.11, 35.1, 200, "dusgh7031@gmail.com");

        assertThat(searchResult.size()).isEqualTo(0);
    }

    /*
       쓰레기통 심사와 관련된 로직 (승인/대기/거절)
       타입별로 검색이 제대로 작동하는지 확인하기 위해서 타입별 테스트 진행
     */

    @DisplayName("승인된 쓰레기통은 검색 결과에 포함돼야 한다(담배꽁초)")
    @Test
    void 승인된_쓰레기통_검색_담배꽁초() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.025104317477, 37.496636817721), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration.getId());

        List<SearchResult> search = searchService.search(BinType.CIGAR, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

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

    @DisplayName("승인된 쓰레기통은 검색 결과에 포함돼야 한다(일반 쓰레기)")
    @Test
    void 승인된_쓰레기통_검색_일반_쓰레기() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.GENERAL, PointUtil.getPoint(127.029123181305, 37.497969565176), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration.getId());

         List<SearchResult> search = searchService.search(BinType.GENERAL, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(5);

        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 382",
                "address");
        assertThat(search).extracting("title").containsExactly(
                "우성아파트O3",
                "우성아파트I3",
                "강남대륭빌딩 앞",
                "강남역2번출구 앞",
                "쓰레기통12");
        assertThat(search).extracting("type").containsExactly(
                BinType.GENERAL,
                BinType.GENERAL,
                BinType.GENERAL,
                BinType.GENERAL,
                BinType.GENERAL);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false, false, false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.02801011993398, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.02862705831201, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.029123181305, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.497969565176, yTolerance);
                });
    }

    /*
       승인 대기 상황 테스트
     */

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(타입 검색x)")
    @Test
    void 승인된_쓰레기통_검색결과_전체() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.025104317477, 37.496636817721), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration.getId());

        List<SearchResult> search = searchService.search(null, 127.027722755059, 37.4956241314633, 300, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(12);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42",
                "address"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "우성아파트O3",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남대륭빌딩 앞",
                "강남대륭빌딩 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18",
                "쓰레기통12"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.GENERAL,
                BinType.RECYCLE,
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
                false,
                false,
                false
        );

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isEqualTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isEqualTo(127.028224355185, xTolerance);
                    assertThat(xList.get(11)).isEqualTo(127.025104317477, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isEqualTo(37.49402562647, yTolerance);
                    assertThat(yList.get(11)).isEqualTo(37.496636817721, yTolerance);
                });
    }

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(담배꽁초)")
    @Test
    void 심사_중인_쓰레기통_검색결과_타입() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통 12", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        List<SearchResult> search = searchService.search(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

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

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(타입 검색x)")
    @Test
    void 심사_중인_쓰레기통_검색결과_전체() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin1 = new Bin("쓰레기통 12", BinType.BEVERAGE, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin1);

        Bin bin2 = new Bin("쓰레기통 123", BinType.GENERAL, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);

        Bin bin3 = new Bin("쓰레기통 1234", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);

        BinRegistration binRegistration = new BinRegistration(user, bin1, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        BinRegistration binRegistration2 = new BinRegistration(user, bin2, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration2);

        BinRegistration binRegistration3 = new BinRegistration(user, bin3, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration3);

        List<SearchResult> search = searchService.search(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(11);

        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "우성아파트O3",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남대륭빌딩 앞",
                "강남대륭빌딩 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
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
                    assertThat(xList.get(3)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isEqualTo(37.49402562647, yTolerance);
                });
    }


    @DisplayName("승인을 거절한 쓰레기통은 검색 결과에서 제외돼야 한다.")
    @Test
    void 승인_거절_쓰레기통_검색결과() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통 12", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        List<SearchResult> search = searchService.search(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");


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
                    assertThat((Double) distanceList.get(0)).isLessThan((Double) distanceList.get(1));
                });

    }

    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(타입 검색x)")
    @Test
    void 승인_거절_쓰레기통_검색결과_전체() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통 12", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        List<SearchResult> search = searchService.search(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(11);
        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "우성아파트O3",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남대륭빌딩 앞",
                "강남대륭빌딩 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
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
                    assertThat(xList.get(3)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isEqualTo(37.49402562647, yTolerance);
                });
    }
    //반경 300에 떨어진게 200m에서 잡히지 않게


    //비회원 로그인 좋아요 다 false


    /*
     입력값 테스트
     */

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(1) ")
    @Test
    void 잘못된_경도_위도_테스트1() {
        assertThatThrownBy(() ->
                searchService.search(BinType.CIGAR, 124.5555, 32.99999999999, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(2) ")
    @Test
    void 잘못된_경도_위도_테스트2() {
        assertThatThrownBy(() ->
                searchService.search(BinType.CIGAR, 123.9999999999999, 33.3332, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(3) ")
    @Test
    void 잘못된_경도_위도_테스트3() {
        assertThatThrownBy(() ->
                searchService.search(BinType.CIGAR, 0.1, 35.111, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(4) ")
    @Test
    void 잘못된_경도_위도_테스트4() {
        assertThatThrownBy(() ->
                searchService.search(BinType.CIGAR, 125.2, 0.1, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(5) ")
    @Test
    void 잘못된_경도_위도_테스트5() {
        assertThatThrownBy(() ->
                searchService.search(BinType.CIGAR, 133.002211, 34.5, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("한국의 경도와 위도를 벗어나면 검색이 실패한다.(6)")
    @Test
    void 잘못된_경도_위도_테스트6() {
        assertThatThrownBy(() ->
                searchService.search(BinType.CIGAR, 132.65, 44.000001, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("검색은 근방 500M내에 있는 것들만 조회된다.")
    @Test
    void 검색_거리_제한() {
        assertThatThrownBy(() ->
                searchService.search(BinType.CIGAR, 132.65, 44.000001, 200, "dusgh7031@gmail.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("잘못된 좌표입니다.");
    }

    @DisplayName("쓰레기통은 반경 500M내에 있는 것들만 조회된다.(타입검색)")
    @Test
    void 쓰레기통_조회_최대_반경_타입_검색() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.025104317477, 37.496636817721), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통123", BinType.CIGAR, PointUtil.getPoint(127.027400362129, 37.499975707358), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);
        Bin bin3 = new Bin("500M이상1", BinType.CIGAR, PointUtil.getPoint(127.02698490849698, 37.500394140109), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);
        Bin bin4 = new Bin("500M이상2", BinType.CIGAR, PointUtil.getPoint(127.021046142735, 37.495347098463), "address4", 0L, 0L, 0L, null, null);
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

        List<SearchResult> search = searchService.search(BinType.CIGAR, 127.027722755059, 37.4956241314633, 1000, "dusgh7031@gmail.com");

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
                    assertThat((Double) distanceList.get(0)).isLessThan(500.0);
                    assertThat((Double) distanceList.get(1)).isLessThan(500.0);
                    assertThat((Double) distanceList.get(2)).isLessThan(500.0);
                    assertThat((Double) distanceList.get(3)).isLessThan(500.0);
                });

    }

    @DisplayName("쓰레기통은 반경 500M내에 있는 것들만 조회된다.(전체검색)")
    @Test
    void 쓰레기통_조회_최대_반경() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.030992140879, 37.4897066749491), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
        Bin bin2 = new Bin("쓰레기통123", BinType.BEVERAGE, PointUtil.getPoint(127.025096790584, 37.502191973002), "address2", 0L, 0L, 0L, null, null);
        binRepository.save(bin2);
        Bin bin3 = new Bin("500M이상1", BinType.GENERAL, PointUtil.getPoint(127.02698490849698, 37.500394140109), "address3", 0L, 0L, 0L, null, null);
        binRepository.save(bin3);
        Bin bin4 = new Bin("500M이상2", BinType.RECYCLE, PointUtil.getPoint(127.021046142735, 37.495347098463), "address4", 0L, 0L, 0L, null, null);
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

        List<SearchResult> search = searchService.search(null, 127.027722755059, 37.4956241314633, 1000, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(11);

        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "우성아파트O3",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남대륭빌딩 앞",
                "강남대륭빌딩 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
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
                    assertThat(xList.get(3)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isEqualTo(37.49402562647, yTolerance);
                });
    }

    @DisplayName("검색 반경을 200m로 제한하면, 200m내에 있는 쓰레기통만 조회된다(타입검색)")
    @Test
    void 쓰레기통_조회_200m_반경_타입_검색() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.025104317477, 37.496636817721), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);

        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());

        List<SearchResult> search = searchService.search(BinType.CIGAR, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

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
                    assertThat((Double) distanceList.get(0)).isLessThan((Double) distanceList.get(1));
                });
    }

    @DisplayName("검색 반경을 200m로 제한하면, 200m내에 있는 쓰레기통만 조회된다(전체 검색)")
    @Test
    void 쓰레기통_조회_200m_반경_전체_검색() {
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통12", BinType.CIGAR, PointUtil.getPoint(127.029588346617, 37.492625668276), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration1 = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration1);
        adminBinRegistrationService.approveRegistration("admin@email.com", binRegistration1.getId());

        List<SearchResult> search = searchService.search(null, 127.027722755059, 37.4956241314633, 200, "dusgh7031@gmail.com");

        assertThat(search.size()).isEqualTo(11);

        assertThat(search).extracting("address").containsExactly(
                "서울 서초구 서초대로78길 24",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 373",
                "서울 서초구 강남대로 365",
                "서울 서초구 강남대로 359",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 362",
                "서울 강남구 강남대로 382",
                "서울 강남구 강남대로 382",
                "서울 서초구 서초대로78길 42"
        );
        assertThat(search).extracting("title").containsExactly(
                "서초동 1327-5",
                "우성아파트O3",
                "던킨도너츠 앞",
                "우성아파트I3",
                "도씨에빛 1 앞",
                "티월드 앞",
                "강남대륭빌딩 앞",
                "강남대륭빌딩 앞",
                "강남역2번출구 앞",
                "강남역2번출구 앞",
                "서초동 1330-18"
        );
        assertThat(search).extracting("type").containsExactlyInAnyOrder(
                BinType.CIGAR,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.GENERAL,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.RECYCLE,
                BinType.GENERAL,
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
                    assertThat(xList.get(3)).isEqualTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isEqualTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isEqualTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isEqualTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isEqualTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isEqualTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isEqualTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isEqualTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isEqualTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isEqualTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isEqualTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isEqualTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isEqualTo(37.49402562647, yTolerance);
                });
    }

    //반환 갯수 제한하기



    //좋아요 뜨게 하도록
}