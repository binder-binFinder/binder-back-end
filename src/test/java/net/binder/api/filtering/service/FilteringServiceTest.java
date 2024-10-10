package net.binder.api.filtering.service;


import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import net.binder.api.filtering.dto.CurseCheckResult;
import net.binder.api.filtering.entity.Curse;
import net.binder.api.filtering.repository.CurseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FilteringServiceTest {

    @Autowired
    private CurseRepository curseRepository;

    @Autowired
    private FilteringService filteringService;

    @Test
    @DisplayName("비속어가 포함됐는지 여부와 비속어로 분류된 단어 목록을 확인할 수 있다.")
    void checkCurse() throws JsonProcessingException {
        //when
        CurseCheckResult curseCheckResult1 = filteringService.checkCurse("시1발");
        CurseCheckResult curseCheckResult2 = filteringService.checkCurse("개1새1끼");
        CurseCheckResult curseCheckResult3 = filteringService.checkCurse("이건 욕설이 아니에요");

        //then
        assertThat(curseCheckResult1.getIsCurse()).isTrue();
        assertThat(curseCheckResult1.getWords()).isNotEmpty();

        assertThat(curseCheckResult2.getIsCurse()).isTrue();
        assertThat(curseCheckResult2.getWords()).isNotEmpty();

        assertThat(curseCheckResult3.getIsCurse()).isFalse();
        assertThat(curseCheckResult3.getWords()).isEmpty();
    }

    @Test
    @DisplayName("DB에 걸러지지 않는 욕설은 AI에 걸러진 뒤 DB에 저장된다.")
    void checkCurse_hasNewWords() throws JsonProcessingException {
        //when
        CurseCheckResult curseCheckResult1 = filteringService.checkCurse("시1발");
        CurseCheckResult curseCheckResult2 = filteringService.checkCurse("개1새1끼");
        CurseCheckResult curseCheckResult3 = filteringService.checkCurse("이건 욕설이 아니에요");

        //then
        List<Curse> curses = curseRepository.findAll();

        assertThat(curses).extracting(Curse::getWord)
                .contains("시1발", "개1새1끼")
                .doesNotContain("이건 욕설이 아니에요");

        assertThat(curseCheckResult1.isAiChecked()).isTrue();
        assertThat(curseCheckResult2.isAiChecked()).isTrue();
        assertThat(curseCheckResult3.isAiChecked()).isTrue();
    }

    @Test
    @DisplayName("DB에 의해 걸러진 욕설은 AI의 검증을 받지 않는다.")
    void checkCurse_hasOldWords() throws JsonProcessingException {
        //given
        curseRepository.save(new Curse("시1발"));
        curseRepository.save(new Curse("개1새1끼"));

        //when
        CurseCheckResult curseCheckResult1 = filteringService.checkCurse("시1발");
        CurseCheckResult curseCheckResult2 = filteringService.checkCurse("개1새1끼");
        CurseCheckResult curseCheckResult3 = filteringService.checkCurse("이건 욕설이 아니에요");

        //then
        assertThat(curseCheckResult1.isAiChecked()).isFalse();
        assertThat(curseCheckResult2.isAiChecked()).isFalse();
        assertThat(curseCheckResult3.isAiChecked()).isTrue();
    }
}