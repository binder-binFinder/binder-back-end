package net.binder.api.filtering.service;


import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.binder.api.filtering.dto.CurseCheckResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FilteringServiceTest {

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
}