package net.binder.api.search.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.search.dto.SearchDto;
import net.binder.api.search.dto.SearchResult;
import org.springframework.stereotype.Repository;

import java.util.List;

import static net.binder.api.bin.entity.QBin.bin;
import static net.binder.api.binregistration.entity.QBinRegistration.binRegistration;
import static net.binder.api.bookmark.entity.QBookmark.bookmark;

@Repository
@RequiredArgsConstructor
public class SearchQueryRepository {

    private static int RADIS_FOR_KEYWORD_SEARCH = 500;
    private static final int MAX_SIZE_OF_SEARCH_RESULT = 20;

    private final JPAQueryFactory jpaQueryFactory;

    public List<SearchResult> findBins(SearchDto searchDto, Long memberId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (searchDto.getType() != null) {
            booleanBuilder.and(bin.type.eq(searchDto.getType()));
        }
        String point = String.format("POINT(%.12f %.12f)", searchDto.getLongitude(), searchDto.getLatitude());
        String geoFunction = "ST_CONTAINS(ST_BUFFER(ST_GeomFromText({0}, 4326), {1}), point)";
        JPAQuery<SearchResult> query = jpaQueryFactory
                .select(Projections.constructor(SearchResult.class,
                        bin.id,
                        bin.address,
                        bin.title,
                        bin.type,
                        Expressions.numberTemplate(Double.class, "ST_Y({0})", bin.point),
                        Expressions.numberTemplate(Double.class, "ST_X({0})", bin.point),
                        ExpressionUtils.as(
                                memberId != null ?
                                        JPAExpressions.selectOne()
                                                .from(bookmark)
                                                .where(bookmark.bin.id.eq(bin.id)
                                                        .and(bookmark.member.id.eq(memberId)))
                                                .exists()
                                        : Expressions.constant(false),
                                "isBookmarked"
                        ),
                        Expressions.numberTemplate(Double.class,
                                "ST_Distance({0}, ST_GeomFromText({1}, 4326))",
                                bin.point, point).as("distance")
                ))
                .from(bin)
                .leftJoin(bin.binRegistration, binRegistration)
                .where(booleanBuilder.and(
                        Expressions.booleanTemplate(geoFunction, point, searchDto.getRadius()
                                )
                                .and(binRegistration.isNull().or(binRegistration.status.eq(BinRegistrationStatus.APPROVED)
                                )).and(bin.deletedAt.isNull())))
                .orderBy(Expressions.numberTemplate(Double.class,
                        "ST_Distance({0}, ST_GeomFromText({1}, 4326))",
                        bin.point, point).asc())
                .limit(MAX_SIZE_OF_SEARCH_RESULT);

        return query.fetch();
    }

    public List<SearchResult> findBinsByKeyword(
            Double longitude,
            Double latitude,
            Double targetLongitude,
            Double targetLatitude,
            Long memberId) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        String currentPoint = String.format("POINT(%.12f %.12f)", latitude, longitude);
        String targetPoint = String.format("POINT(%.12f %.12f)", targetLatitude, targetLongitude);
        String geoFunction = "ST_CONTAINS(ST_BUFFER(ST_GeomFromText({0}, 4326), {1}), point)";
        JPAQuery<SearchResult> query = jpaQueryFactory
                .select(Projections.constructor(SearchResult.class,
                        bin.id,
                        bin.address,
                        bin.title,
                        bin.type,
                        Expressions.numberTemplate(Double.class, "ST_Y({0})", bin.point),
                        Expressions.numberTemplate(Double.class, "ST_X({0})", bin.point),
                        ExpressionUtils.as(
                                memberId != null ?
                                        JPAExpressions.selectOne()
                                                .from(bookmark)
                                                .where(bookmark.bin.id.eq(bin.id)
                                                        .and(bookmark.member.id.eq(memberId)))
                                                .exists()
                                        : Expressions.constant(false),
                                "isBookmarked"
                        ),
                        Expressions.numberTemplate(Double.class,
                                "ST_Distance({0}, ST_GeomFromText({1}, 4326))",
                                bin.point, currentPoint).as("distance")
                ))
                .from(bin)
                .leftJoin(bin.binRegistration, binRegistration)
                .where(booleanBuilder.and(
                        Expressions.booleanTemplate(geoFunction, targetPoint, RADIS_FOR_KEYWORD_SEARCH
                                )
                                .and(binRegistration.isNull().or(binRegistration.status.eq(BinRegistrationStatus.APPROVED)
                                )).and(bin.deletedAt.isNull())))
                .orderBy(Expressions.numberTemplate(Double.class,
                        "ST_Distance({0}, ST_GeomFromText({1}, 4326))",
                        bin.point, currentPoint).asc())
                .limit(MAX_SIZE_OF_SEARCH_RESULT);
        return query.fetch();
    }
}

