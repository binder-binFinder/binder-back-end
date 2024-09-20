package net.binder.api.comment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CommentInfoForMember {

    private final Boolean isWriter;

    private final Boolean isLiked;

    private final Boolean isDisliked;
}
