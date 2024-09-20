package net.binder.api.comment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.entity.Comment;
import net.binder.api.comment.repository.CommentRepository;
import net.binder.api.comment.repository.CommentSort;
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

    public static final int PAGE_SIZE = 20;

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

        if (email == null) { // 비로그인 유저일 경우
            Comment comment = getComment(commentId);
            return CommentDetail.createForNoMember(comment);
        }

        // 로그인 유저일 경우
        Member member = memberService.findByEmail(email);

        CommentDetail commentDetail = commentRepository.findCommentDetail(commentId, member.getId());

        if (commentDetail == null) {
            throw new BadRequestException("존재하지 않는 댓글입니다.");
        }
        return commentDetail;

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

    @Transactional(readOnly = true)
    public List<CommentDetail> getCommentDetails(String email, Long binId, CommentSort sort, Long lastCommentId,
                                                 Long lastLikeCount) {
        validateSearchCondition(sort, lastCommentId, lastLikeCount);

        Bin bin = binService.findById(binId);

        if (email == null) { // 비로그인 유저일 경우
            return commentRepository.findCommentDetails(bin.getId(), sort, lastCommentId, lastLikeCount, PAGE_SIZE);
        }

        //로그인 유저일 경우
        Member member = memberService.findByEmail(email);

        return commentRepository.findCommentDetails(member.getId(), bin.getId(), sort,
                lastCommentId, lastLikeCount, PAGE_SIZE);
    }

    private void validateSearchCondition(CommentSort sort, Long lastCommentId, Long lastLikeCount) {
        if (sort == CommentSort.LIKE_COUNT_DESC) {
            if ((lastLikeCount == null && lastCommentId != null) || (lastLikeCount != null && lastCommentId == null)) {
                throw new BadRequestException(
                        "정렬 조건이 좋아요순(LIKE_COUNT_DESC)일 경우, lastCommentId와 lastLikeCount는 둘 다 제공되거나 둘 다 제공되지 않아야 합니다.");
            }
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
