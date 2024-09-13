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
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import net.binder.api.common.kakaomap.service.KakaoMapService;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.member.service.MemberService;
import net.binder.api.search.dto.SearchDto;
import net.binder.api.search.dto.SearchResult;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
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
                new PublicBinData("우성아파트O3", "서울 서초구 강남대로 373", BinType.GENERAL, null),
                new PublicBinData("던킨도너츠 앞", "서울 서초구 강남대로 373", BinType.RECYCLE, null),
                new PublicBinData("우성아파트I3", "서울 서초구 강남대로 373", BinType.GENERAL, null),
                new PublicBinData("강남대륭빌딩 앞", "서울 강남구 강남대로 362", BinType.RECYCLE, null),
                new PublicBinData("강남대륭빌딩 앞", "서울 강남구 강남대로 362", BinType.GENERAL, null),
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

    @DisplayName("담배꽁초 쓰레기통을 검색하면 주변에 있는 쓰레기통을 찾을 수 있다.")
    @Test
    void 담배꽁초_쓰레기통_검색() {

        SearchDto searchDto = new SearchDto(BinType.CIGAR, 127.027722755059, 37.4956241314633,200);
        List<SearchResult> search = searchService.search(searchDto, "dusgh7031@gmail.com");


        assertThat(search.size()).isEqualTo(2);

        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028224355185, xTolerance);
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

    @DisplayName("비로그인 이용자도 쓰레기통 검색을 할 수 있다.")
    @Test
    void 비로그인_쓰레기통_검색(){

        SearchDto searchDto = new SearchDto(BinType.CIGAR, 127.027722755059, 37.4956241314633,200);
        List<SearchResult> search = searchService.search(searchDto, null);


        assertThat(search.size()).isEqualTo(2);

        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028224355185, xTolerance);
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

    @Test
    void test2() {
        SearchDto searchDto = new SearchDto(null, 127.027722755059, 37.4956241314633, 200);
        List<SearchResult> search = searchService.search(searchDto, "dusgh7031@gmail.com");


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
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isCloseTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isCloseTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isCloseTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isCloseTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isCloseTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isCloseTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isCloseTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isCloseTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isCloseTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isCloseTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isCloseTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isCloseTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isCloseTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isCloseTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isCloseTo(37.49402562647, yTolerance);
                });
    }

    @DisplayName("검색 결과의 첫번째 쓰레기통이 현재 위치에서 가장 가까운 쓰레기통이다.")
    @Test
    void 쓰레기통_거리(){
        SearchDto searchDto = new SearchDto(null, 127.027722755059, 37.4956241314633, 200);
        List<SearchResult> searchResult = searchService.search(searchDto, "dusgh7031@gmail.com");
        Double distanceOfFirstBins=searchResult.get(0).getDistance();

        for (int i = 0; i < searchResult.size() - 1; i++) {
            assertThat(searchResult.get(i).getDistance()).isLessThanOrEqualTo(searchResult.get(i + 1).getDistance());
        }

        for(int i =0; i<searchResult.size(); i++){
            assertThat(distanceOfFirstBins).isLessThanOrEqualTo(searchResult.get(i).getDistance());
        }
    }

    @DisplayName("DB에 저장되지 않은 위도, 경도로 조회하면 검색결과가 나오지 않는다.")
    @Test
    void DB_데이터_좌표_기준_검색(){

        //일본의 경도, 위도 좌표 예시

        SearchDto searchDto = new SearchDto(null, 139.7454, 35.6586, 200);
        List<SearchResult> searchResult = searchService.search(searchDto, "dusgh7031@gmail.com");

        assertThat(searchResult.size()).isEqualTo(0);
    }


    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(특정 타입 검색)")
    @Test
    void 승인된_쓰레기통_검색(){
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통 12", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        SearchDto searchDto = new SearchDto(BinType.CIGAR, 127.027722755059, 37.4956241314633,200);
        List<SearchResult> search = searchService.search(searchDto, "dusgh7031@gmail.com");


        assertThat(search.size()).isEqualTo(2);

        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028224355185, xTolerance);
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


    @DisplayName("심사 중인 쓰레기통은 검색 결과에서 제외돼야 한다.(특정 타입 검색)")
    @Test
    void 심사_중인_쓰레기통_검색결과_타입(){
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통 12", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        SearchDto searchDto = new SearchDto(BinType.CIGAR, 127.027722755059, 37.4956241314633,200);
        List<SearchResult> search = searchService.search(searchDto, "dusgh7031@gmail.com");


        assertThat(search.size()).isEqualTo(2);

        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028224355185, xTolerance);
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
    void 심사_중인_쓰레기통_검색결과_전체() {

        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통 12", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);

        SearchDto searchDto = new SearchDto(null, 127.027722755059, 37.4956241314633,200);
        List<SearchResult> search = searchService.search(searchDto, "dusgh7031@gmail.com");


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
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isCloseTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isCloseTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isCloseTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isCloseTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isCloseTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isCloseTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isCloseTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isCloseTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isCloseTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isCloseTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isCloseTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isCloseTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isCloseTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isCloseTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isCloseTo(37.49402562647, yTolerance);
                });
    }


    @DisplayName("승인을 거절한 쓰레기통은 검색 결과에서 제외돼야 한다.")
    @Test
    void 승인_거절_쓰레기통_검색결과(){
        Member admin = new Member("admin@email.com", "admin", Role.ROLE_ADMIN, null);
        Member user = new Member("user@email.com", "user", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(admin, user));

        Bin bin = new Bin("쓰레기통 12", BinType.CIGAR, PointUtil.getPoint(127.027722755059, 37.4956241314633), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);

        BinRegistration binRegistration = new BinRegistration(user, bin, BinRegistrationStatus.PENDING);
        binRegistrationRepository.save(binRegistration);
        adminBinRegistrationService.rejectRegistration("admin@email.com", binRegistration.getId(), "거절 사유");

        SearchDto searchDto = new SearchDto(BinType.CIGAR, 127.027722755059, 37.4956241314633,200);
        List<SearchResult> search = searchService.search(searchDto, "dusgh7031@gmail.com");


        assertThat(search.size()).isEqualTo(2);

        assertThat(search).extracting("address").containsExactly("서울 서초구 서초대로78길 24", "서울 서초구 서초대로78길 42");
        assertThat(search).extracting("title").containsExactly("서초동 1327-5", "서초동 1330-18");
        assertThat(search).extracting("type").containsExactly(BinType.CIGAR, BinType.CIGAR);
        assertThat(search).extracting("isBookMarked").containsExactly(false, false);

        assertThat(search).extracting(SearchResult::getLongitude)
                .satisfies(xList -> {
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028224355185, xTolerance);
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

        SearchDto searchDto = new SearchDto(null, 127.027722755059, 37.4956241314633,200);
        List<SearchResult> search = searchService.search(searchDto, "dusgh7031@gmail.com");


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
                    assertThat(xList.get(0)).isCloseTo(127.027752353367, xTolerance);
                    assertThat(xList.get(1)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(2)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(3)).isCloseTo(127.028010119934, xTolerance);
                    assertThat(xList.get(4)).isCloseTo(127.028348895147, xTolerance);
                    assertThat(xList.get(5)).isCloseTo(127.02860980273, xTolerance);
                    assertThat(xList.get(6)).isCloseTo(127.029416048324, xTolerance);
                    assertThat(xList.get(7)).isCloseTo(127.029416048324, xTolerance);
                    assertThat(xList.get(8)).isCloseTo(127.028627058312, xTolerance);
                    assertThat(xList.get(9)).isCloseTo(127.028627058312, xTolerance);
                    assertThat(xList.get(10)).isCloseTo(127.028224355185, xTolerance);
                });

        assertThat(search).extracting(SearchResult::getLatitude)
                .satisfies(yList -> {
                    assertThat(yList.get(0)).isCloseTo(37.495544565616, yTolerance);
                    assertThat(yList.get(1)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(2)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(3)).isCloseTo(37.495982934664, yTolerance);
                    assertThat(yList.get(4)).isCloseTo(37.495323407006, yTolerance);
                    assertThat(yList.get(5)).isCloseTo(37.494798958122, yTolerance);
                    assertThat(yList.get(6)).isCloseTo(37.495305576475, yTolerance);
                    assertThat(yList.get(7)).isCloseTo(37.495305576475, yTolerance);
                    assertThat(yList.get(8)).isCloseTo(37.49704867762, yTolerance);
                    assertThat(yList.get(9)).isCloseTo(37.49704867762, yTolerance);
                    assertThat(yList.get(10)).isCloseTo(37.49402562647, yTolerance);
                });
    }

    @DisplayName("좋아요를 누른 쓰레기통이 검색결과에 나온다.")
    @Test
    void 검색_결과_쓰레기통_좋아요_표시(){}

    //비회원 로그인 좋아요 다 false

    //잘못된 타입
}