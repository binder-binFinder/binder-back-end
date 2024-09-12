package net.binder.api.common.kakaomap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import org.springframework.beans.factory.annotation.Value;
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

    public List<ProcessedBinData> getPoints(List<PublicBinData> list) {
        return list.parallelStream()
                .map(this::getPoint)
                .filter(Objects::nonNull)
                .toList();
    }

    public ProcessedBinData getPoint(PublicBinData initialBinData) {
        String body = fetchDataFromApi(initialBinData);
        return parseResponse(initialBinData, body);
    }

    private String fetchDataFromApi(PublicBinData initialBinData) {
        RequestEntity<Void> req = RequestEntity
                .get(SEARCH_URL + QUERY_PARAM + initialBinData.getAddress())
                .header("Authorization", KAKAO_MAP_KEY)
                .build();
        return restTemplate.exchange(req, String.class).getBody();
    }

    private ProcessedBinData parseResponse(PublicBinData initialBinData, String body) {
        try {
            JsonNode rootNode = objectMapper.readTree(body);
            JsonNode documentsNode = rootNode.path("documents");
            if (documentsNode.isArray() && !documentsNode.isEmpty()) {
                JsonNode firstDocument = documentsNode.get(0);
                return getProcessedBinData(initialBinData, firstDocument);
            }
        } catch (IOException | NumberFormatException e) {
        }
        return null;
    }

    private static ProcessedBinData getProcessedBinData(PublicBinData initialBinData, JsonNode firstDocument) {
        JsonNode roadAddressNode = firstDocument.path("road_address");
        JsonNode addressNode = firstDocument.path("address");
        double x, y;
        String address;



        if (roadAddressNode.isEmpty()) {
            x = Double.parseDouble(addressNode.path("x").asText());
            y = Double.parseDouble(addressNode.path("y").asText());
            address = addressNode.path("address_name").asText();
        } else {
            x = Double.parseDouble(roadAddressNode.path("x").asText());
            y = Double.parseDouble(roadAddressNode.path("y").asText());
            address = roadAddressNode.path("address_name").asText();
        }

        return ProcessedBinData.from(initialBinData, x, y, address);
    }
}
