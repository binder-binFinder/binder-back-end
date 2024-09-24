package net.binder.api.comment.repository;

import net.binder.api.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);

    @Modifying
    int deleteByCommentIdAndMemberId(Long commentId, Long memberId);
}
