package net.binder.api.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.dto.CreateCommentRequest;
import net.binder.api.comment.dto.CreateCommentResponse;
import net.binder.api.comment.dto.GetCommentDetailResponse;
import net.binder.api.comment.dto.GetCommentListResponse;
import net.binder.api.comment.dto.ModifyCommentRequest;
import net.binder.api.comment.repository.CommentSort;
import net.binder.api.comment.service.CommentService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Tag(name = "쓰레기통 댓글 관리")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "쓰레기통 댓글 작성")
    @PostMapping
    public CreateCommentResponse createComment(@CurrentUser String email,
                                               @Valid @RequestBody CreateCommentRequest request) {

        Long commentId = commentService.createComment(email, request.getBinId(), request.getContent());

        return new CreateCommentResponse(commentId);
    }

    @Operation(summary = "쓰레기통 댓글 상세 조회")
    @GetMapping("/{id}")
    public GetCommentDetailResponse getCommentDetail(@CurrentUser String email, @PathVariable Long id) {

        CommentDetail commentDetail = commentService.getCommentDetail(email, id);

        return new GetCommentDetailResponse(commentDetail);
    }

    @Operation(summary = "쓰레기통 댓글 수정", description = "작성자 본인이 아닐 경우 예외가 발생합니다.")
    @PatchMapping("/{id}")
    public void modifyComment(@CurrentUser String email, @PathVariable Long id,
                              @Valid @RequestBody ModifyCommentRequest request) {

        commentService.modifyComment(email, id, request.getContent());
    }

    @Operation(summary = "쓰레기통 댓글 삭제", description = "작성자 본인이 아닐 경우 예외가 발생합니다.")
    @DeleteMapping("/{id}")
    public void deleteComment(@CurrentUser String email, @PathVariable Long id) {

        commentService.deleteComment(email, id);
    }

    @Operation(summary = "쓰레기통 댓글 목록 조회", description = "정렬 조건이 좋아요순(LIKE_COUNT_DESC)일 경우, lastCommentId와 lastLikeCount는 둘 다 제공되거나 둘 다 제공되지 않아야 합니다.")
    @GetMapping
    public GetCommentListResponse getCommentList(
            @CurrentUser String email,
            @RequestParam @NotNull Long binId,
            @RequestParam(defaultValue = "CREATED_AT_DESC") CommentSort sort,
            @RequestParam(required = false) Long lastCommentId,
            @RequestParam(required = false) Long lastLikeCount) {

        List<CommentDetail> commentDetails = commentService.getCommentDetails(email, binId, sort, lastCommentId,
                lastLikeCount);
        return new GetCommentListResponse(commentDetails);
    }

    @Operation(
            summary = "쓰레기통 댓글 좋아요",
            description = "이미 좋아요를 누른 상태이면 400 예외가 발생하고, 이미 싫어요를 누른 상태이면 싫어요가 삭제됩니다.")
    @PostMapping("/{id}/like")
    public void createCommentLike(@CurrentUser String email, @PathVariable Long id) {
        commentService.createCommentLike(email, id);
    }
}
