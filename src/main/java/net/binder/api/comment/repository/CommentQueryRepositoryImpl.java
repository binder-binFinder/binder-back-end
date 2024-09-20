package net.binder.api.comment.repository;

import static net.binder.api.comment.entity.QComment.comment;
import static net.binder.api.comment.entity.QCommentDislike.commentDislike;
import static net.binder.api.comment.entity.QCommentLike.commentLike;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.dto.CommentInfoForMember;

@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public CommentDetail findCommentDetail(Long commentId, Long memberId) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                CommentDetail.class,
                                comment.id,
                                comment.bin.id,
                                comment.member.nickname,
                                comment.content,
                                comment.likeCount,
                                comment.dislikeCount,
                                comment.createdAt,

                                Projections.constructor(
                                        CommentInfoForMember.class,
                                        comment.member.id.eq(memberId),
                                        commentLike.isNotNull(),
                                        commentDislike.isNotNull()
                                )

                        ))
                .from(comment)
                .leftJoin(commentLike).on(comment.eq(commentLike.comment).and(commentLike.member.id.eq(memberId)))
                .leftJoin(commentDislike)
                .on(comment.eq(commentDislike.comment).and(commentDislike.member.id.eq(memberId)))
                .where(comment.id.eq(commentId))
                .fetchOne();
    }

    @Override
    public List<CommentDetail> findCommentDetails(Long memberId, Long binId, CommentSort sort, Long lastCommentId,
                                                  Long lastLikeCount,
                                                  int pageSize) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                CommentDetail.class,
                                comment.id,
                                comment.bin.id,
                                comment.member.nickname,
                                comment.content,
                                comment.likeCount,
                                comment.dislikeCount,
                                comment.createdAt,

                                Projections.constructor(
                                        CommentInfoForMember.class,
                                        comment.member.id.eq(memberId),
                                        commentLike.isNotNull(),
                                        commentDislike.isNotNull()
                                )
                        )
                )
                .from(comment)
                .leftJoin(commentLike).on(commentLike.comment.eq(comment).and(commentLike.member.id.eq(memberId)))
                .leftJoin(commentDislike)
                .on(commentDislike.comment.eq(comment).and(commentDislike.member.id.eq(memberId)))
                .where(getWhere(binId, sort, lastCommentId, lastLikeCount))
                .limit(pageSize)
                .orderBy(getOrderSpecifiers(sort))
                .fetch();
    }

    @Override
    public List<CommentDetail> findCommentDetails(Long binId, CommentSort sort, Long lastCommentId,
                                                  Long lastLikeCount,
                                                  int pageSize) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                CommentDetail.class,
                                comment.id,
                                comment.bin.id,
                                comment.member.nickname,
                                comment.content,
                                comment.likeCount,
                                comment.dislikeCount,
                                comment.createdAt,
                                Expressions.nullExpression(CommentInfoForMember.class)
                        )
                )
                .from(comment)
                .where(getWhere(binId, sort, lastCommentId, lastLikeCount))
                .limit(pageSize)
                .orderBy(getOrderSpecifiers(sort))
                .fetch();
    }

    private BooleanBuilder getWhere(Long binId, CommentSort sort, Long lastCommentId, Long lastLikeCount) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(comment.bin.id.eq(binId));

        if (sort == CommentSort.LIKE_COUNT_DESC && lastLikeCount != null && lastCommentId != null) {
            return booleanBuilder.and(comment.likeCount.lt(lastLikeCount)
                    .or(comment.likeCount.eq(lastLikeCount)
                            .and(comment.id.lt(lastCommentId))));
        }
        if (sort == CommentSort.CREATED_AT_DESC && lastCommentId != null) {
            return booleanBuilder.and(comment.id.lt(lastCommentId));
        }

        return booleanBuilder;
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(CommentSort sort) {
        if (sort == CommentSort.LIKE_COUNT_DESC) {
            return new OrderSpecifier[]{comment.likeCount.desc(), comment.id.desc()};
        }
        return new OrderSpecifier[]{comment.id.desc()};
    }
}
