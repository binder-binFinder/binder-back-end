package net.binder.api.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.common.entity.BaseEntityWithSoftDelete;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Comment extends BaseEntityWithSoftDelete {

    public static final int MAX_CONTENT_LENGTH = 60;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Bin bin;

    private String content;

    private Long likeCount;

    private Long dislikeCount;

    @Builder
    public Comment(Member member, Bin bin, String content) {
        validateContentLength(content);
        this.member = member;
        this.bin = bin;
        this.content = content;
        this.likeCount = 0L;
        this.dislikeCount = 0L;
    }

    private void validateContentLength(String content) {
        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw new BadRequestException("댓글 글자 수는 60자 이하여야 합니다.");
        }
    }
}
