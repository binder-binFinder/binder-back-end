package net.binder.api.comment.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.entity.Comment;
import net.binder.api.comment.repository.CommentRepository;
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

    public CommentDetail createComment(String email, Long binId, String content) {
        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);

        Comment comment = new Comment(member, bin, content);

        commentRepository.save(comment);

        return CommentDetail.from(comment, true); // 자신이 생성한 코멘트이므로 isOwner -> true
    }
}
