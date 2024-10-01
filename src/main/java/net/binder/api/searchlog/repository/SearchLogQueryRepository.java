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

        BooleanBuilder builder = createBaseCondition(email);
         if (lastSearchLogId != null ) {
            builder.and(searchLog.id.gt(lastSearchLogId));
        }

        return jpaQueryFactory
                .select(Projections.constructor(SearchLogItem.class,
                        searchLog.id,
                        searchLog.keyword,
                        searchLog.address,
                        searchLog.hasBookmarkedBin,
                        searchLog.hasBinsNearby))
                .from(searchLog)
                .where(builder)
                .orderBy(searchLog.id.asc())
                .limit(10)
                .fetch();
    }

    private BooleanBuilder createBaseCondition(String email) {
        return new BooleanBuilder()
                .and(searchLog.member.email.eq(email))
                .and(searchLog.deletedAt.isNull());
    }
}
