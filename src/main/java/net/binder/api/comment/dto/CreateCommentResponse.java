package net.binder.api.comment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CreateCommentResponse {

    private final Long commentId;
}
