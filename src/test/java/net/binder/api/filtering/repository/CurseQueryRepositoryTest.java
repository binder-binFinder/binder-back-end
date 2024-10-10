package net.binder.api.filtering.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.binder.api.filtering.entity.Curse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CurseQueryRepositoryTest {

    @Autowired
    private CurseRepository curseRepository;

    @Autowired
    private CurseQueryRepository curseQueryRepository;

    @Test
    @DisplayName("대상 구문에 포함된 욕설 목록을 찾을 수 있다.")
    void findWordsInSentence() {
        //given
        curseRepository.saveAll(List.of(new Curse("바보"), new Curse("멍청")));

        //when
        List<String> words = curseQueryRepository.findWordsInSentence("이바보야진짜아니야멍청아");

        //then
        assertThat(words).contains("바보", "멍청");
    }
}