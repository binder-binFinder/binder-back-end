package net.binder.api.comment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class GetCommentDetailResponse {

    private final CommentDetail commentDetail;
}
