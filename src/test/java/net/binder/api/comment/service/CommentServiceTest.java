package net.binder.api.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CommentServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private CommentService commentService;

    private Member member;

    private Bin bin;

    @BeforeEach
    void setUp() {
        member = new Member("member@email.com", "member", Role.ROLE_USER, null);
        memberRepository.save(member);

        bin = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
    }

    @Test
    @DisplayName("댓글이 생성되면 댓글 상세 정보를 반환한다.")
    void createComment_success() {
        //when
        CommentDetail commentDetail = commentService.createComment(member.getEmail(), bin.getId(), "댓글");

        //then
        assertThat(commentDetail.getCommentId()).isNotNull();
        assertThat(commentDetail.getBinId()).isEqualTo(bin.getId());
        assertThat(commentDetail.getIsOwner()).isEqualTo(true);
        assertThat(commentDetail.getCreatedAt()).isNotNull();
        assertThat(commentDetail.getWriter()).isEqualTo(member.getNickname());
        assertThat(commentDetail.getContent()).isEqualTo("댓글");
        assertThat(commentDetail.getLikeCount()).isEqualTo(0);
        assertThat(commentDetail.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("댓글 글자수는 60자를 초과할 수 없다.")
    void createComment_fail_maxLength() {
        String test = "a".repeat(61);
        assertThatThrownBy(() -> commentService.createComment(member.getEmail(), bin.getId(), test))
                .isInstanceOf(BadRequestException.class);
    }
}