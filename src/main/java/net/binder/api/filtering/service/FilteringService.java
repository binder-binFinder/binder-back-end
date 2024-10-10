package net.binder.api.filtering.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.binder.api.filtering.dto.CurseCheckResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class FilteringService {
    private final CurseReader curseReader;

    private final CurseManager curseManager;

    private final AiCheckManager aiCheckManager;

    public FilteringService(CurseReader curseReader,
                            CurseManager curseManager,
                            AiCheckManager aiCheckManager) {
        this.curseReader = curseReader;
        this.curseManager = curseManager;
        this.aiCheckManager = aiCheckManager;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CurseCheckResult checkCurse(String target) throws JsonProcessingException {
        // DB에서 검증
        List<String> dbCheckCurseWords = curseReader.readWordsInTarget(target);

        // 욕설 목록이 있다면 결과 반환
        if (!dbCheckCurseWords.isEmpty()) {
            return new CurseCheckResult(true, dbCheckCurseWords, false);
        }

        // DB에서 검증을 못한 경우 AI에게 검증 요청
        CurseCheckResult curseCheckResult = aiCheckManager.requestAiCheck(target);

        // 욕설이 없는 경우 결과 그대로 반환
        if (!curseCheckResult.isCurse()) {
            return curseCheckResult;
        }

        // 욕설이 있는 경우 AI의 욕설 목록에서 새로운 단어만 추출하여 DB에 저장
        List<String> aiCheckCurseWords = curseCheckResult.getWords();

        List<String> newCurseWords = curseReader.findNewCurseWords(aiCheckCurseWords);

        curseManager.addWords(newCurseWords);

        return curseCheckResult;
    }
}
