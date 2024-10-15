package net.binder.api.common.kakaomap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import org.springframework.beans.factory.annotation.Value;
import org.locationtech.jts.geom.Point;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoMapService {
    private static final String SEARCH_URL = "https://dapi.kakao.com/v2/local/search/address.json?";
    private static final String QUERY_PARAM = "query=";

    @Value("${kakaomap.appkey}")
    private String KAKAO_MAP_KEY;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<ProcessedBinData> getProcessedBins(List<PublicBinData> list) {
        return list.parallelStream()
                .map(this::getProcessBin)
                .filter(Objects::nonNull)
                .toList();
    }

    public Point getPoint(String address) {
        String body = fetchDataFromApi(address);
        JsonNode document = parseDocument(body);

        if(document==null){
            throw new BadRequestException("입력된 주소는 존재하지 않습니다.");
        }

        JsonNode roadAddressNode = getNode(document, "road_address");
        JsonNode addressNode = getNode(document, "address");

        if (!roadAddressNode.isEmpty() && addressNode.path("x").asText().isBlank()) {
            return PointUtil.getPoint(getX(roadAddressNode), getY(roadAddressNode));
        }
        return PointUtil.getPoint(getX(addressNode), getY(addressNode));
    }

    public ProcessedBinData getProcessBin(PublicBinData initialBinData) {
        String body = fetchDataFromApi(initialBinData.getAddress());
        JsonNode document = parseDocument(body);
        if (document == null) {
            return null;
        }
        return getBinInfo(initialBinData, document);
    }

    private String fetchDataFromApi(String address) {
        RequestEntity<Void> req = RequestEntity
                .get(SEARCH_URL + QUERY_PARAM + address)
                .header("Authorization", KAKAO_MAP_KEY)
                .build();
        return restTemplate.exchange(req, String.class).getBody();
    }

    private JsonNode parseDocument(String body) {
        try {
            JsonNode rootNode = objectMapper.readTree(body);
            JsonNode documentsNode = rootNode.path("documents");
            if (documentsNode.isArray() && !documentsNode.isEmpty()) {
                return documentsNode.get(0);
            }
        } catch (IOException | NumberFormatException e) {
        }
        return null;
    }

    private ProcessedBinData getBinInfo(PublicBinData initialBinData, JsonNode document) {
        JsonNode roadAddressNode = getNode(document, "road_address");
        JsonNode addressNode = getNode(document, "address");

        if (!roadAddressNode.isEmpty() && !addressNode.path("x").asText().isBlank()) {
            return ProcessedBinData.from(initialBinData, getX(roadAddressNode), getY(roadAddressNode), getNode(roadAddressNode, "address_name").asText());
        }

        return ProcessedBinData.from(initialBinData, getX(addressNode), getY(addressNode), getNode(addressNode, "address_name").asText());
    }

    private static JsonNode getNode(JsonNode document, String title) {
        return document.path(title);
    }

    private double getY(JsonNode addressNode) {
        return Double.parseDouble(addressNode.path("y").asText());
    }

    private double getX(JsonNode addressNode) {
        return Double.parseDouble(addressNode.path("x").asText());
    }
}
