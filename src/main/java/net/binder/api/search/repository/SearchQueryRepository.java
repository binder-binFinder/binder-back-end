package net.binder.api.search.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.binder.api.search.dto.SearchDto;
import net.binder.api.search.dto.SearchResult;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.binder.api.bin.entity.QBin.bin;
import static net.binder.api.bookmark.entity.QBookmark.bookmark;

@Repository
@RequiredArgsConstructor
public class SearchQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<SearchResult> findBins(SearchDto searchDto, int radius, Long memberId) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (searchDto.getType() != null) {
            booleanBuilder.and(bin.type.eq(searchDto.getType()));
        }

        String target = String.format("POINT(%.12f %.13f)", searchDto.getLongitude(), searchDto.getLatitude());
        String geoFunction = "ST_CONTAINS(ST_BUFFER(ST_GeomFromText('%s', 4326), {0}), point)";
        String expression = String.format(geoFunction, target);

        return jpaQueryFactory
                .select(Projections.constructor(SearchResult.class,
                        bin.id,
                        bin.address,
                        bin.bookmarkCount,
                        bin.dislikeCount,
                        bin.likeCount,
                        bin.imageUrl,
                        bin.title,
                        bin.type,
                        bin.point,
                        Expressions.cases()
                                .when(bookmark.id.isNotNull())
                                .then(true)
                                .otherwise(false),
                        Expressions.numberTemplate(Double.class,
                                "ST_Distance({0}, ST_GeomFromText({1}, 4326))",
                                bin.point, target)
                ))
                .from(bin)
                .leftJoin(bookmark).on(bin.id.eq(bookmark.bin.id).and(bookmark.member.id.eq(memberId)))
                .where(booleanBuilder.and(
                        Expressions.booleanTemplate(expression, radius, bin.point)
                ))
                .orderBy(Expressions.numberTemplate(Double.class,
                        "ST_Distance({0}, ST_GeomFromText({1}, 4326))",
                        bin.point, target).asc())
                .fetch();

        //pending인거 제외하기
    }
}
