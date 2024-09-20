package net.binder.api.comment.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.entity.Comment;
import net.binder.api.comment.repository.CommentRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class CommentService {

    private final MemberService memberService;

    private final BinService binService;

    private final CommentRepository commentRepository;

    public Long createComment(String email, Long binId, String content) {
        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);

        Comment comment = new Comment(member, bin, content);

        commentRepository.save(comment);

        return comment.getId();
    }

    @Transactional(readOnly = true)
    public CommentDetail getCommentDetail(String email, Long commentId) {

        Comment comment = getComment(commentId);

        boolean isOwner = comment.getMember().isOwnEmail(email);

        return CommentDetail.from(comment, isOwner);
    }

    public void modifyComment(String email, Long commentId, String content) {
        Comment comment = getComment(commentId);
        validateIsWriter(email, comment);
        comment.modifyContent(content);
    }

    public void deleteComment(String email, Long commentId) {
        Comment comment = getComment(commentId);
        validateIsWriter(email, comment);

        boolean isDeleted = comment.softDelete();

        if (!isDeleted) {
            throw new BadRequestException("이미 삭제된 댓글입니다.");
        }
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));
    }

    private void validateIsWriter(String email, Comment comment) {
        if (!comment.isWriter(email)) {
            throw new BadRequestException("작성자 본인만 댓글 수정이 가능합니다.");
        }
    }
}
