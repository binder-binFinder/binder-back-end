package net.binder.api.searchlog.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.binder.api.searchlog.dto.SearchLogItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import static net.binder.api.searchlog.entity.QSearchLog.searchLog;

@RequiredArgsConstructor
@Repository
public class SearchLogQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<SearchLogItem> findSearchLogByMember(
            String email,
            Long lastSearchLogId){

        BooleanBuilder builder = createBaseCondition(email, lastSearchLogId);
        return jpaQueryFactory
                .select(Projections.constructor(SearchLogItem.class,
                        searchLog.id,
                        searchLog.keyword,
                        searchLog.address,
                        searchLog.hasBinsNearby,
                        searchLog.createdAt))
                .from(searchLog)
                .where(builder)
                .orderBy(searchLog.id.desc())
                .limit(10)
                .fetch();
    }

    private BooleanBuilder createBaseCondition(String email, Long lastSearchLogId) {
        BooleanBuilder builder = new BooleanBuilder()
                .and(searchLog.member.email.eq(email))
                .and(searchLog.deletedAt.isNull());
        if (lastSearchLogId != null ) {
            builder.and(searchLog.id.lt(lastSearchLogId));
        }
        return builder;
    }
}
