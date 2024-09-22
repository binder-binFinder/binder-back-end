package net.binder.api.bookmark.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinType;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.search.dto.SearchResult;
import org.springframework.data.repository.query.Param;
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
            Long bookmarkId){

        BooleanBuilder builder = new BooleanBuilder();

        if(bookmarkId !=null){
            builder.and(bookmark.id.gt(bookmarkId));
        }

        String point = String.format("POINT(%.12f %.12f)", latitude, longitude);

        List<BookmarkResponse> result = jpaQueryFactory
                .select(Projections.fields(BookmarkResponse.class,
                        bookmark.id.as("bookmarkId"),
                        bin.id.as("binId"),
                        bin.address,
                        bin.title,
                        bin.type.as("binType"),
                        Expressions.numberTemplate(Double.class,
                                "ST_Distance({0}, ST_GeomFromText({1}, 4326))",
                                bin.point, point).as("distance")))
                .from(bookmark)
                .leftJoin(bookmark.bin, bin)
                .leftJoin(bookmark.member, member)
                .where(builder.and(member.email.eq(email)))
                .orderBy(bookmark.id.asc())
                .limit(10)
                .fetch();

        return Optional.of(result);
    }






}
