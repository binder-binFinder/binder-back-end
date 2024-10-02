package net.binder.api.bookmark.repository;

import com.querydsl.core.BooleanBuilder;import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;import net.binder.api.bookmark.dto.BookmarkResponse;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static net.binder.api.bin.entity.QBin.bin;
import static net.binder.api.bookmark.entity.QBookmark.bookmark;
import static net.binder.api.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class BookmarkQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public Optional<List<BookmarkResponse>> findBookmarksByMember(
            String email,
            Double longitude,
            Double latitude,
            Long lastBookmarkId,
            Double lastDistance){

        BooleanBuilder builder = createBaseCondition(email);
        NumberExpression<Double> distance = calculateDistance(latitude, longitude);

        if (lastBookmarkId != null && lastDistance != null) {
            builder.and(createPaginationCondition(lastBookmarkId, lastDistance, distance));
        }

        List<BookmarkResponse> result = jpaQueryFactory
                .select(Projections.constructor(BookmarkResponse.class,
                        bookmark.id.as("bookmarkId"),
                        bin.id.as("binId"),
                        bin.address,
                        bin.title,
                        bin.type.as("binType"),
                        Expressions.numberTemplate(Double.class, "ST_Y({0})", bin.point),
                        Expressions.numberTemplate(Double.class, "ST_X({0})", bin.point),
                        distance.as("distance")))
                .from(bookmark)
                .leftJoin(bookmark.bin, bin)
                .leftJoin(bookmark.member, member)
                .where(builder)
                .orderBy(distance.asc(), bookmark.id.asc())
                .limit(10)
                .fetch();

        return Optional.of(result);
    }

    private BooleanBuilder createBaseCondition(String email) {
        return new BooleanBuilder()
                .and(member.email.eq(email))
                .and(bin.deletedAt.isNull());
    }

    private NumberExpression<Double> calculateDistance(Double latitude, Double longitude) {
        String point = String.format("POINT(%.12f %.12f)", latitude, longitude);
        return Expressions.numberTemplate(Double.class,
                "ST_Distance({0}, ST_GeomFromText({1}, 4326))", bin.point, point);
    }

    private BooleanExpression createPaginationCondition(Long lastBookmarkId, Double lastDistance, NumberExpression<Double> distance) {
        return bookmark.id.gt(lastBookmarkId).and(distance.goe(lastDistance))
                .or(bookmark.id.lt(lastBookmarkId).and(distance.gt(lastDistance)));
    }
}
