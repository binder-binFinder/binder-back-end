package net.binder.api.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.comment.dto.CommentDetail;
import net.binder.api.comment.entity.Comment;
import net.binder.api.comment.entity.CommentDislike;
import net.binder.api.comment.entity.CommentLike;
import net.binder.api.comment.repository.CommentDislikeRepository;
import net.binder.api.comment.repository.CommentLikeRepository;
import net.binder.api.comment.repository.CommentRepository;
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

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentDislikeRepository commentDislikeRepository;

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
    @DisplayName("댓글이 생성되면 댓글 id를 반환한다.")
    void createComment_success() {
        //when
        Long commentId = commentService.createComment(member.getEmail(), bin.getId(), "댓글");

        //then
        assertThat(commentId).isNotNull();
    }

    @Test
    @DisplayName("댓글 글자수는 60자를 초과할 수 없다.")
    void createComment_fail_maxLength() {
        String test = "a".repeat(61);
        assertThatThrownBy(() -> commentService.createComment(member.getEmail(), bin.getId(), test))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("댓글 상세정보를 조회할 수 있다. 비로그인 유저일 경우 info는 null이다.")
    void getCommentDetail_NoMember() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when
        CommentDetail commentDetail = commentService.getCommentDetail(null, comment.getId());

        //then
        assertThat(commentDetail.getCommentId()).isNotNull();
        assertThat(commentDetail.getBinId()).isEqualTo(bin.getId());
        assertThat(commentDetail.getCreatedAt()).isNotNull();
        assertThat(commentDetail.getWriter()).isEqualTo(member.getNickname());
        assertThat(commentDetail.getContent()).isEqualTo("댓글");
        assertThat(commentDetail.getLikeCount()).isEqualTo(0);
        assertThat(commentDetail.getDislikeCount()).isEqualTo(0);
        assertThat(commentDetail.getCommentInfoForMember()).isNull();
    }

    @Test
    @DisplayName("댓글 상세정보를 조회할 수 있다. 다른 사람이 작성한 댓글일 경우 isOwner는 false이다.")
    void getCommentDetail_isWriter_False() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        Member member2 = new Member("member2@email.com", "member2", Role.ROLE_USER, null);
        memberRepository.save(member2);

        //when
        CommentDetail commentDetail = commentService.getCommentDetail(member2.getEmail(), comment.getId());

        //then
        assertThat(commentDetail.getCommentId()).isNotNull();
        assertThat(commentDetail.getBinId()).isEqualTo(bin.getId());
        assertThat(commentDetail.getCreatedAt()).isNotNull();
        assertThat(commentDetail.getWriter()).isEqualTo(member.getNickname());
        assertThat(commentDetail.getContent()).isEqualTo("댓글");
        assertThat(commentDetail.getLikeCount()).isEqualTo(0);
        assertThat(commentDetail.getDislikeCount()).isEqualTo(0);
        assertThat(commentDetail.getCommentInfoForMember().getIsWriter()).isFalse();
        assertThat(commentDetail.getCommentInfoForMember().getIsLiked()).isFalse();
        assertThat(commentDetail.getCommentInfoForMember().getIsDisliked()).isFalse();
    }

    @Test
    @DisplayName("댓글 상세정보를 조회할 수 있다. 본인이 작성한 댓글일 경우 isOwner는 true이다.")
    void getCommentDetail_isWriter_True() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when
        CommentDetail commentDetail = commentService.getCommentDetail(member.getEmail(), comment.getId());

        //then
        assertThat(commentDetail.getCommentId()).isNotNull();
        assertThat(commentDetail.getBinId()).isEqualTo(bin.getId());
        assertThat(commentDetail.getCreatedAt()).isNotNull();
        assertThat(commentDetail.getWriter()).isEqualTo(member.getNickname());
        assertThat(commentDetail.getContent()).isEqualTo("댓글");
        assertThat(commentDetail.getLikeCount()).isEqualTo(0);
        assertThat(commentDetail.getDislikeCount()).isEqualTo(0);
        assertThat(commentDetail.getCommentInfoForMember().getIsWriter()).isTrue();
        assertThat(commentDetail.getCommentInfoForMember().getIsLiked()).isFalse();
        assertThat(commentDetail.getCommentInfoForMember().getIsDisliked()).isFalse();
    }

    @Test
    @DisplayName("댓글 상세정보를 조회할 수 있다. 해당 글을 좋아요 할 경우 isLiked는 true이다.")
    void getCommentDetail_isLiked_True() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        commentLikeRepository.save(new CommentLike(member, comment));

        //when
        CommentDetail commentDetail = commentService.getCommentDetail(member.getEmail(), comment.getId());

        //then
        assertThat(commentDetail.getCommentId()).isNotNull();
        assertThat(commentDetail.getBinId()).isEqualTo(bin.getId());
        assertThat(commentDetail.getCreatedAt()).isNotNull();
        assertThat(commentDetail.getWriter()).isEqualTo(member.getNickname());
        assertThat(commentDetail.getContent()).isEqualTo("댓글");
        assertThat(commentDetail.getLikeCount()).isEqualTo(0);
        assertThat(commentDetail.getDislikeCount()).isEqualTo(0);
        assertThat(commentDetail.getCommentInfoForMember().getIsWriter()).isTrue();
        assertThat(commentDetail.getCommentInfoForMember().getIsLiked()).isTrue();
        assertThat(commentDetail.getCommentInfoForMember().getIsDisliked()).isFalse();
    }

    @Test
    @DisplayName("댓글 상세정보를 조회할 수 있다. 해당 글을 싫어요 할 경우 isdisliked는 true이다.")
    void getCommentDetail_isDisliked_True() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        commentDislikeRepository.save(new CommentDislike(member, comment));

        //when
        CommentDetail commentDetail = commentService.getCommentDetail(member.getEmail(), comment.getId());

        //then
        assertThat(commentDetail.getCommentId()).isNotNull();
        assertThat(commentDetail.getBinId()).isEqualTo(bin.getId());
        assertThat(commentDetail.getCreatedAt()).isNotNull();
        assertThat(commentDetail.getWriter()).isEqualTo(member.getNickname());
        assertThat(commentDetail.getContent()).isEqualTo("댓글");
        assertThat(commentDetail.getLikeCount()).isEqualTo(0);
        assertThat(commentDetail.getDislikeCount()).isEqualTo(0);
        assertThat(commentDetail.getCommentInfoForMember().getIsWriter()).isTrue();
        assertThat(commentDetail.getCommentInfoForMember().getIsLiked()).isFalse();
        assertThat(commentDetail.getCommentInfoForMember().getIsDisliked()).isTrue();
    }

    @Test
    @DisplayName("작성자 본인이라면 댓글 내용을 수정할 수 있다.")
    void modifyComment_success() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when
        commentService.modifyComment(member.getEmail(), comment.getId(), "수정");

        //then
        assertThat(comment.getContent()).isEqualTo("수정");
    }

    @Test
    @DisplayName("댓글 수정시 길이가 60자를 초과하면 예외가 발생한다.")
    void modifyComment_fail_contentLength() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when&then
        String newContent = "a".repeat(61);
        assertThatThrownBy(() -> commentService.modifyComment(member.getEmail(), comment.getId(), newContent))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("타인이 댓글 수정을 요청하면 예외가 발생한다.")
    void modifyComment_fail_isNotWriter() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));
        Member member2 = new Member("member2@email.com", "member2", Role.ROLE_USER, null);
        memberRepository.save(member2);

        //when & then
        assertThatThrownBy(() -> commentService.modifyComment(member2.getEmail(), comment.getId(), "수정"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("작성자 본인이라면 댓글을 삭제할 수 있다.")
    void deleteComment_success() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when
        commentService.deleteComment(member.getEmail(), comment.getId());

        //then
        assertThat(comment.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("타인이 댓글 삭제를 요청하면 예외가 발생한다.")
    void deleteComment_fail_isNotWriter() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        Member member2 = new Member("member2@email.com", "member2", Role.ROLE_USER, null);
        memberRepository.save(member2);

        //when & then
        assertThatThrownBy(() -> commentService.deleteComment(member2.getEmail(), comment.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("이미 삭제된 댓글을 다시 삭제 요청하면 예외가 발생한다.")
    void deleteComment_fail_isAlreadyDeleted() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));
        commentService.deleteComment(member.getEmail(), comment.getId());

        //when & then
        assertThatThrownBy(() -> commentService.deleteComment(member.getEmail(), comment.getId()))
                .isInstanceOf(BadRequestException.class);
    }
}