package net.binder.api.comment.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.binder.api.comment.entity.Comment;

@RequiredArgsConstructor
@Getter
@Builder
public class CommentDetail {

    private final Long commentId;

    private final Long binId;

    private final String writer;

    private final String content;

    private final Long likeCount;

    private final Long dislikeCount;

    private final LocalDateTime createdAt;

    private final CommentInfoForMember commentInfoForMember;

    public static CommentDetail createForNoMember(Comment comment) {

        return CommentDetail.builder()
                .commentId(comment.getId())
                .binId(comment.getBin().getId())
                .writer(comment.getMember().getNickname())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .dislikeCount(comment.getDislikeCount())
                .createdAt(comment.getCreatedAt())
                .build();
    }

}
