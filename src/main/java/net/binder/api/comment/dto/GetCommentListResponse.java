package net.binder.api.comment.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class GetCommentListResponse {

    private final List<CommentDetail> commentDetails;
}
