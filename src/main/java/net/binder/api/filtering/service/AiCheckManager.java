package net.binder.api.filtering.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.binder.api.filtering.dto.CurseCheckResult;
import net.binder.api.filtering.dto.OpenAiRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AiCheckManager {

    private final RestTemplate restTemplate;

    private final String openAiUrl;

    private final String openAiModel;

    private final String openAiKey;

    private final ObjectMapper objectMapper;


    public AiCheckManager(
            RestTemplate restTemplate,
            @Value("${openai.api.url}") String openAiUrl,
            @Value("${openai.model}") String openAiModel,
            @Value("${openai.api.key}") String openAiKey, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.openAiUrl = openAiUrl;
        this.openAiModel = openAiModel;
        this.openAiKey = openAiKey;
        this.objectMapper = objectMapper;
    }

    public CurseCheckResult requestAiCheck(String target) throws JsonProcessingException {
        log.debug("AI 검증을 시작합니다. target = {}", target);
        RequestEntity<OpenAiRequest> request = getOpenAiRequest(
                target);

        String body = restTemplate.exchange(request, String.class).getBody();

        CurseCheckResult curseCheckResult = getCurseCheckResult(body); // GPT의 검증 결과

        if (!curseCheckResult.getIsCurse()) {
            return curseCheckResult;
        }

        List<String> words = curseCheckResult.getWords(); // GPT가 감지한 욕설 목록

        if (isMatched(target, words)) { // target(본문)이 AI가 찾아낸 욕설을 포함하고 있는 경우
            log.debug("GPT 정상 target = {}, words = {}", target, String.join(",", words));
            return curseCheckResult;
        }
        // 포함하지 않을 경우
        log.error("GPT 오류 target = {}, words = {}", target, String.join(",", words));
        return new CurseCheckResult(false, List.of(), true);
    }


    private RequestEntity<OpenAiRequest> getOpenAiRequest(String target) {
        return RequestEntity
                .post(openAiUrl)
                .header("Authorization", "Bearer " + openAiKey)
                .body(new OpenAiRequest(openAiModel, target));
    }

    private CurseCheckResult getCurseCheckResult(String body) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(body);

        String content = root.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();

        CurseCheckResult curseCheckResult = objectMapper.readValue(content, CurseCheckResult.class);
        return new CurseCheckResult(curseCheckResult.isCurse(), curseCheckResult.getWords(), true);
    }

    private boolean isMatched(String target, List<String> words) {

        for (String word : words) {
            if (target.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
