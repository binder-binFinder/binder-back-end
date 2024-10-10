package net.binder.api.filtering.repository;

import static net.binder.api.filtering.entity.QCurse.curse;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CurseQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<String> findWordsInSentence(String sentence) {
        return jpaQueryFactory
                .select(curse.word)
                .from(curse)
                .where(containsWord(sentence, curse.word))
                .fetch();
    }

    public List<String> findExistingWords(List<String> words) {
        return jpaQueryFactory
                .select(curse.word)
                .from(curse)
                .where(curse.word.in(words))
                .fetch();
    }

    private Predicate containsWord(String target, StringPath word) {

        return Expressions.booleanTemplate("{0} LIKE CONCAT('%', {1}, '%')", target, word);
    }
}
