package net.binder.api.comment.repository;

import net.binder.api.comment.entity.CommentDislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface CommentDislikeRepository extends JpaRepository<CommentDislike, Long> {

    @Modifying
    int deleteByCommentIdAndMemberId(Long commentId, Long memberId);

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);
}
