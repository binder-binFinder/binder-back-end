package net.binder.api.comment.repository;

import java.util.List;
import net.binder.api.comment.dto.CommentDetail;

public interface CommentQueryRepository {

    List<CommentDetail> findCommentDetails(Long memberId, Long binId, CommentSort sort, Long lastCommentId,
                                           Long lastLikeCount,
                                           int pageSize);

    CommentDetail findCommentDetail(Long commentId, Long memberId);

    List<CommentDetail> findCommentDetails(Long binId, CommentSort sort, Long lastCommentId,
                                           Long lastLikeCount,
                                           int pageSize);
}
