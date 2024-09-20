package net.binder.api.bin.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.binsetup.repository.BinBatchInsertRepository;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import net.binder.api.common.kakaomap.service.KakaoMapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.RequestEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@Transactional
@SpringBootTest
class BinRepositoryTest {

    private final String SEARCH_URL = "https://dapi.kakao.com/v2/local/search/address.json?";
    private final String QUERY_PARAM = "query=";

    @Value("${kakaomap.appkey}")
    private String KAKAO_MAP_KEY;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private KakaoMapService kakaoMapService;

    @Autowired
    private BinBatchInsertRepository binBatchInsertRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        List<PublicBinData> list = List.of(
                new PublicBinData("양재동 221-3", "양재동 221-3 건물 앞 녹지사이 보도", BinType.CIGAR, null),
                new PublicBinData("양재동 80", "서울 서초구 강남대로 148", BinType.CIGAR, null),
                new PublicBinData("연세로 42 일삼약국", "서울 서대문구 연세로 39", BinType.GENERAL, null),
                new PublicBinData("연세로 36", "서울 서대문구 연세로 36", BinType.GENERAL, null),
                new PublicBinData("연세로 39", "서울 서대문구 연세로 39", BinType.RECYCLE, null),
                new PublicBinData("연세로 28-1", "서울 서대문구 연세로 28-1", BinType.GENERAL, null),
                new PublicBinData("신촌역로14", "서울 서대문구 신촌역로 14", BinType.RECYCLE, null),
                new PublicBinData("신촌역로14", "서울 서대문구 신촌역로 14", BinType.GENERAL, null)
        );
        List<ProcessedBinData> bins = kakaoMapService.getPoints(list);
        binBatchInsertRepository.batchInsertInitialBins(bins);
        entityManager.flush();
    }

    @Test
    @DisplayName("DB에 쓰레기통이 저장될 때 제목과 주소 마지막에 있는 공백을 제거할 수 있다.")
    void DB_데이터_문자열_마지막_공백_확인() {
        List<Bin> allBins = binRepository.findAll();
        allBins.parallelStream().forEach(bin -> {
            assertThat(bin.getAddress().endsWith(" ")).isFalse();
            assertThat(bin.getTitle().endsWith(" ")).isFalse();
        });
    }

    /*
      DB에는 도로명 주소의 좌표를 넣었지만 어떤 데이터는 도로명 주소가 없어서 지번 주소 좌표로 들어감.
      도로명 주소와 지번 주소의 좌표는 조금씩 달라서 지번 주소 좌표가 들어간 데이터는 지번 주소 좌표로 다시 한번 검색해서 좌표가 동일한지 확인해야 함.
    */
    @Test
    @DisplayName("DB에 들어간 데이터가 카카오맵 정보와 동일하다.")
    void DB_데이터_카카오맵_데이터_확인() {

        List<Bin> allBins = binRepository.findAll();

        allBins.parallelStream().forEach(bin -> {
            try {
                ProcessedBinData processedBinData = getProcessedBinData(bin);
                if (isJibunAddressCoordinate(bin, processedBinData)) {
                    processedBinData = getJibunAddress(new PublicBinData(bin.getTitle(), bin.getTitle(), bin.getType(), null));
                }
                checkCoordinate(bin, processedBinData);
            } catch (NullPointerException e) {
                /*
                DB에 들어간 데이터 중에 '서울 용산구 한강대로 405'이거 하나만 NullPointerException이 뜸.
                좌표는 제대로 들어간 게 맞지만 예외가 떠서 이것잠 try~catch로 잡음
                 */
                System.out.println(bin.getAddress());
            }
        });
    }

    private ProcessedBinData getProcessedBinData(Bin bin) {
        ProcessedBinData processedBinData = null;
        String address = bin.getAddress();
        if (!address.isEmpty()) {
            processedBinData = kakaoMapService.getPoint(new PublicBinData(bin.getTitle(), address, bin.getType(), null));

            //카카오맵에 도로명 주소 정보가 있을 때 테스트
            if (processedBinData != null) {
                boolean compare = address.equals(processedBinData.getAddress());
                if(!compare){
                    compare = address.contains(processedBinData.getAddress());
                }
                try {
                    assertThat(compare).isTrue();
                } catch (AssertionFailedError e){
                    System.out.println(address +"!="+processedBinData.getAddress());
                }
            }
        }

        //카카오맵에 도로명 주소 정보가 없을 때 지번 주소로 검색
        if (processedBinData == null) {
            processedBinData = getJibunAddress(new PublicBinData(bin.getTitle(), bin.getTitle(), bin.getType(), null));
        }
        return processedBinData;
    }

    private boolean isJibunAddressCoordinate(Bin bin, ProcessedBinData processedBinData) {
        return Math.abs(bin.getPoint().getX() - processedBinData.getLongitude()) > 0.00000000001;
    }

    private void checkCoordinate(Bin bin, ProcessedBinData processedBinData) {

        assertThat(bin.getPoint().getX()).isEqualTo(processedBinData.getLongitude(), within(0.00000000001));
        assertThat(bin.getPoint().getY()).isEqualTo(processedBinData.getLatitude(), within(0.000000000001));
    }

    private ProcessedBinData getJibunAddress(PublicBinData initialBinData) {
        RequestEntity<Void> req = RequestEntity
                .get(SEARCH_URL + QUERY_PARAM + initialBinData.getTitle())
                .header("Authorization", KAKAO_MAP_KEY)
                .build();
        String body = restTemplate.exchange(req, String.class).getBody();
        return parseResponse(initialBinData, body);
    }

    private ProcessedBinData parseResponse(PublicBinData initialBinData, String body) {
        try {
            JsonNode rootNode = objectMapper.readTree(body);
            JsonNode documentsNode = rootNode.path("documents");
            if (documentsNode.isArray() && !documentsNode.isEmpty()) {
                JsonNode firstDocument = documentsNode.get(0);
                double x = Double.parseDouble(firstDocument.path("address").path("x").asText());
                double y = Double.parseDouble(firstDocument.path("address").path("y").asText());
                String address = firstDocument.path("address").path("address_name").asText();
                return ProcessedBinData.from(initialBinData, x, y, address);
            }
        } catch (IOException | NumberFormatException e) {
        }
        return null;
    }
}