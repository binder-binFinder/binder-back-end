package net.binder.api.comment.repository;

import static net.binder.api.comment.entity.QComment.comment;
import static net.binder.api.comment.entity.QCommentDislike.commentDislike;
import static net.binder.api.comment.entity.QCommentLike.commentLike;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.dto.CommentInfoForMember;

@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CommentDetail> findCommentDetails(Long memberId, Long binId, CommentSort sort, Long lastCommentId) {
        return List.of();
    }

    @Override
    public CommentDetail findCommentDetail(Long commentId, Long memberId) {
        return jpaQueryFactory.select(
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
}
