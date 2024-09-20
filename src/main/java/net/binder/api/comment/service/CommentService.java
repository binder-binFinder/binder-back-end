package net.binder.api.comment.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.entity.Comment;
import net.binder.api.comment.repository.CommentRepository;
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

    public CommentDetail getCommentDetail(String email, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글입니다."));

        boolean isOwner = comment.getMember().isOwnEmail(email);

        return CommentDetail.from(comment, isOwner);
    }
}
