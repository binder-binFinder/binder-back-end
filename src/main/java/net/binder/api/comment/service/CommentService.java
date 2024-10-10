package net.binder.api.comment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.entity.Comment;
import net.binder.api.comment.entity.CommentDislike;
import net.binder.api.comment.entity.CommentLike;
import net.binder.api.comment.repository.CommentDislikeRepository;
import net.binder.api.comment.repository.CommentLikeRepository;
import net.binder.api.comment.repository.CommentRepository;
import net.binder.api.comment.repository.CommentSort;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.filtering.dto.CurseCheckResult;
import net.binder.api.filtering.service.FilteringService;
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

    private final CommentLikeRepository commentLikeRepository;

    private final CommentDislikeRepository commentDislikeRepository;

    private final FilteringService filteringService;

    public Long createComment(String email, Long binId, String content) throws JsonProcessingException {
        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);

        Comment comment = new Comment(member, bin, content); // 60자 이내인지 검사

        validateIsCurse(content);

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

    public void modifyComment(String email, Long commentId, String content) throws JsonProcessingException {
        Comment comment = getComment(commentId);
        validateIsWriter(email, comment);
        validateIsCurse(content);
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

    public void createCommentLike(String email, Long commentId) {
        Member member = memberService.findByEmail(email);
        Comment comment = getCommentWithPessimisticLock(commentId); // 조회와 동시에 배타적 락 획득

        // 이미 좋아요가 존재하는 경우 예외 발생
        validateIsAlreadyLiked(comment, member);

        // 이미 싫어요가 있는 경우 삭제하고 싫어요 1 감소
        decreaseDislikeIfExists(comment, member);

        CommentLike commentLike = new CommentLike(member, comment);
        commentLikeRepository.save(commentLike);
        comment.increaseLikeCount();
    }

    public void createCommentDislike(String email, Long commentId) {
        Member member = memberService.findByEmail(email);
        Comment comment = getCommentWithPessimisticLock(commentId);

        // 이미 싫어요가 존재하는 경우 예외 발생
        validateIsAlreadyDisliked(comment, member);

        // 이미 좋아요가 있는 경우 삭제하고 좋아요 1 감소
        decreaseLikeIfExists(comment, member);

        CommentDislike commentDislike = new CommentDislike(member, comment);
        commentDislikeRepository.save(commentDislike);
        comment.increaseDislikeCount();
    }

    public void deleteCommentLike(String email, Long commentId) {
        Member member = memberService.findByEmail(email);
        Comment comment = getCommentWithPessimisticLock(commentId);

        validateIsNotLiked(comment, member);

        commentLikeRepository.deleteByCommentIdAndMemberId(comment.getId(), member.getId());
        comment.decreaseLikeCount();
    }

    public void deleteCommentDislike(String email, Long commentId) {
        Member member = memberService.findByEmail(email);
        Comment comment = getCommentWithPessimisticLock(commentId);

        validateIsNotDisliked(comment, member);

        commentDislikeRepository.deleteByCommentIdAndMemberId(comment.getId(), member.getId());
        comment.decreaseDislikeCount();
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

    private Comment getCommentWithPessimisticLock(Long commentId) {
        return commentRepository.findByIdWithPessimisticLock(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));
    }

    private void validateIsWriter(String email, Comment comment) {
        if (!comment.isWriter(email)) {
            throw new BadRequestException("작성자 본인만 댓글 수정이 가능합니다.");
        }
    }

    private void validateIsAlreadyLiked(Comment comment, Member member) {
        if (commentLikeRepository.existsByCommentIdAndMemberId(comment.getId(), member.getId())) {
            throw new BadRequestException("이미 좋아요한 댓글입니다.");
        }
    }

    private void validateIsAlreadyDisliked(Comment comment, Member member) {
        if (commentDislikeRepository.existsByCommentIdAndMemberId(comment.getId(), member.getId())) {
            throw new BadRequestException("이미 싫어요한 댓글입니다.");
        }
    }

    private void decreaseDislikeIfExists(Comment comment, Member member) {
        if (commentDislikeRepository.deleteByCommentIdAndMemberId(comment.getId(), member.getId()) != 0) {
            comment.decreaseDislikeCount();
        }
    }

    private void decreaseLikeIfExists(Comment comment, Member member) {
        if (commentLikeRepository.deleteByCommentIdAndMemberId(comment.getId(), member.getId()) != 0) {
            comment.decreaseLikeCount();
        }
    }

    private void validateIsNotLiked(Comment comment, Member member) {
        if (!commentLikeRepository.existsByCommentIdAndMemberId(comment.getId(), member.getId())) {
            throw new BadRequestException("좋아요를 한 내역이 없습니다.");
        }
    }

    private void validateIsNotDisliked(Comment comment, Member member) {
        if (!commentDislikeRepository.existsByCommentIdAndMemberId(comment.getId(), member.getId())) {
            throw new BadRequestException("싫어요를 한 내역이 없습니다.");
        }
    }

    private void validateIsCurse(String content) throws JsonProcessingException {
        CurseCheckResult curseCheckResult = filteringService.checkCurse(content);
        if (curseCheckResult.getIsCurse()) {
            String words = extractCurseWords(curseCheckResult);

            throw new BadRequestException("댓글 내용에 비속어가 포함되어 있습니다. " + words);
        }
    }

    private String extractCurseWords(CurseCheckResult curseCheckResult) {
        return curseCheckResult.getWords()
                .stream()
                .collect(Collectors.joining(", ", "(", ")"));
    }
}
