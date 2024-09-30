package net.binder.api.filtering.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.binder.api.filtering.dto.CurseCheckResult;
import net.binder.api.filtering.dto.OpenAiRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FilteringManager {

    private final RestTemplate restTemplate;

    private final String openAiUrl;

    private final String openAiModel;

    private final String openAiKey;

    private final ObjectMapper objectMapper;

    public FilteringManager(RestTemplate restTemplate, @Value("${openai.api.url}") String openAiUrl,
                            @Value("${openai.model}") String openAiModel,
                            @Value("${openai.api.key}") String openAiKey, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.openAiUrl = openAiUrl;
        this.openAiModel = openAiModel;
        this.openAiKey = openAiKey;
        this.objectMapper = objectMapper;
    }

    public CurseCheckResult checkCurse(String target) throws JsonProcessingException {
        RequestEntity<OpenAiRequest> request = RequestEntity
                .post(openAiUrl)
                .header("Authorization", "Bearer " + openAiKey)
                .body(new OpenAiRequest(openAiModel, target));

        String body = restTemplate.exchange(request, String.class).getBody();

        JsonNode root = objectMapper.readTree(body);

        String content = root.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();

        return objectMapper.readValue(content, CurseCheckResult.class);
    }
}
