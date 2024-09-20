package net.binder.api.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.dto.CreateCommentRequest;
import net.binder.api.comment.dto.CreateCommentResponse;
import net.binder.api.comment.service.CommentService;
import net.binder.api.common.annotation.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

        CommentDetail commentDetail = commentService.createComment(email, request.getBinId(), request.getContent());
        
        return new CreateCommentResponse(commentDetail);
    }
}
