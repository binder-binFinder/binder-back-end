package net.binder.api.common.kakaomap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.binsetup.dto.PublicBinData;
import net.binder.api.common.kakaomap.dto.ProcessedBinData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public Optional<ProcessedBinData> getPoint(PublicBinData initialBinData) {
        String body = fetchDataFromApi(initialBinData);
        ProcessedBinData initData = null;
        return parseResponse(initialBinData, body, initData);
    }

    private String fetchDataFromApi(PublicBinData initialBinData) {
        RequestEntity<Void> req = RequestEntity
                .get(SEARCH_URL + QUERY_PARAM + initialBinData.getAddress())
                .header("Authorization", KAKAO_MAP_KEY)
                .build();
        String body = restTemplate.exchange(req, String.class).getBody();
        return body;
    }

    private Optional<ProcessedBinData> parseResponse(PublicBinData initialBinData, String body, ProcessedBinData initData) {
        try {
            JsonNode rootNode = objectMapper.readTree(body);
            JsonNode documentsNode = rootNode.path("documents");
            if (documentsNode.isArray() && !documentsNode.isEmpty()) {
                JsonNode firstDocument = documentsNode.get(0);
                double x = Double.parseDouble(firstDocument.path("x").asText());
                double y = Double.parseDouble(firstDocument.path("y").asText());
                return Optional.of(ProcessedBinData.from(initialBinData, x, y));
            }
        } catch (IOException | NumberFormatException e) {
        }
        return Optional.empty();
    }
}
