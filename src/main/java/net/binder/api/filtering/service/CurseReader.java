package net.binder.api.filtering.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.filtering.repository.CurseQueryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CurseReader {

    private final CurseQueryRepository curseQueryRepository;

    @Transactional(readOnly = true)
    public List<String> readWordsInTarget(String target) {
        return curseQueryRepository.findWordsInSentence(target);
    }

    @Transactional(readOnly = true)
    public List<String> findNewCurseWords(List<String> words) {
        // 이미 DB에 존재하는 단어
        List<String> existingWords = curseQueryRepository.findExistingWords(words);

        words.removeAll(existingWords);
        return words;
    }
}
