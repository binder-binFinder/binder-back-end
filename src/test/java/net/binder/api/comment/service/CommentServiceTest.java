package net.binder.api.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import net.binder.api.comment.repository.CommentSort;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

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

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        member = new Member("member@email.com", "member", Role.ROLE_USER, null);
        memberRepository.save(member);

        bin = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address", 0L, 0L, 0L, null, null);
        binRepository.save(bin);
    }

    @Test
    @DisplayName("댓글이 생성되면 댓글 id를 반환한다.")
    void createComment_success() throws JsonProcessingException {
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
    void modifyComment_success() throws JsonProcessingException {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when
        commentService.modifyComment(member.getEmail(), comment.getId(), "수정");

        //then
        assertThat(comment.getContent()).isEqualTo("수정");
    }

    @Test
    @DisplayName("댓글 수정시 욕설이 포함되어 있으면 예외가 발생한다.")
    void modifyComment_fail_hasCurse() throws JsonProcessingException {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when
        assertThatThrownBy(() -> commentService.modifyComment(member.getEmail(), comment.getId(), "시1발"))
                .isInstanceOf(BadRequestException.class);
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

    @Test
    @DisplayName("비로그인 사용자가 댓글 목록을 조회할 수 있다.")
    void getCommentDetails_Anonymous() {
        // given
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            comments.add(commentRepository.save(new Comment(member, bin, "댓글" + i)));
        }

        // when
        List<CommentDetail> commentDetails = commentService.getCommentDetails(null, bin.getId(),
                CommentSort.CREATED_AT_DESC, null, null);

        // then
        assertThat(commentDetails).hasSize(20); // PAGE_SIZE is 20
        assertThat(commentDetails.get(0).getCommentId()).isEqualTo(comments.get(24).getId());
        assertThat(commentDetails.get(19).getCommentId()).isEqualTo(comments.get(5).getId());
        assertThat(commentDetails.get(0).getCommentInfoForMember()).isNull();
    }

    @Test
    @DisplayName("좋아요 순으로 댓글 목록을 정렬할 수 있다.")
    void getCommentDetails_SortByLikeCount() {
        // given
        Member other = new Member("other@email.com", "other", Role.ROLE_USER, null);
        memberRepository.save(other);

        Comment comment1 = commentRepository.save(new Comment(member, bin, "댓글1"));
        Comment comment2 = commentRepository.save(new Comment(member, bin, "댓글2"));
        Comment comment3 = commentRepository.save(new Comment(member, bin, "댓글3"));

        commentLikeRepository.save(new CommentLike(member, comment2));
        commentLikeRepository.save(new CommentLike(member, comment3));
        commentLikeRepository.save(
                new CommentLike(other, comment3));

        comment2.increaseLikeCount();
        comment3.increaseLikeCount();
        comment3.increaseLikeCount();
        commentRepository.saveAll(List.of(comment2, comment3));

        // when
        List<CommentDetail> commentDetails = commentService.getCommentDetails(member.getEmail(), bin.getId(),
                CommentSort.LIKE_COUNT_DESC, null, null);

        // then
        assertThat(commentDetails).hasSize(3);
        assertThat(commentDetails.get(0).getCommentId()).isEqualTo(comment3.getId());
        assertThat(commentDetails.get(1).getCommentId()).isEqualTo(comment2.getId());
        assertThat(commentDetails.get(2).getCommentId()).isEqualTo(comment1.getId());
        assertThat(commentDetails.get(0).getLikeCount()).isEqualTo(2);
        assertThat(commentDetails.get(1).getLikeCount()).isEqualTo(1);
        assertThat(commentDetails.get(2).getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("잘못된 커서 값으로 조회 시 예외가 발생한다.")
    void getCommentDetails_InvalidCursor() {
        // given
        commentRepository.save(new Comment(member, bin, "댓글"));

        // when & then
        // 1. LIKE_COUNT_DESC 정렬에서 lastCommentId만 제공
        assertThatThrownBy(() ->
                commentService.getCommentDetails(member.getEmail(), bin.getId(), CommentSort.LIKE_COUNT_DESC, 1L, null)
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining(
                        "정렬 조건이 좋아요순(LIKE_COUNT_DESC)일 경우, lastCommentId와 lastLikeCount는 둘 다 제공되거나 둘 다 제공되지 않아야 합니다.");

        // 2. LIKE_COUNT_DESC 정렬에서 lastLikeCount만 제공
        assertThatThrownBy(() ->
                commentService.getCommentDetails(member.getEmail(), bin.getId(), CommentSort.LIKE_COUNT_DESC, null, 5L)
        ).isInstanceOf(BadRequestException.class)
                .hasMessageContaining(
                        "정렬 조건이 좋아요순(LIKE_COUNT_DESC)일 경우, lastCommentId와 lastLikeCount는 둘 다 제공되거나 둘 다 제공되지 않아야 합니다.");
    }

    @Test
    @DisplayName("댓글 목록을 조회할 때 좋아요와 싫어요 상태가 정확히 반영된다.")
    void getCommentDetails_LikeAndDislikeStatus() {
        // given
        Comment comment1 = commentRepository.save(new Comment(member, bin, "댓글1"));
        Comment comment2 = commentRepository.save(new Comment(member, bin, "댓글2"));
        Comment comment3 = commentRepository.save(new Comment(member, bin, "댓글3"));

        commentLikeRepository.save(new CommentLike(member, comment1));
        commentDislikeRepository.save(new CommentDislike(member, comment2));

        // when
        List<CommentDetail> commentDetails = commentService.getCommentDetails(member.getEmail(), bin.getId(),
                CommentSort.CREATED_AT_DESC, null, null);

        // then
        assertThat(commentDetails).hasSize(3);
        assertThat(commentDetails.get(2).getCommentInfoForMember().getIsLiked()).isTrue();
        assertThat(commentDetails.get(2).getCommentInfoForMember().getIsDisliked()).isFalse();
        assertThat(commentDetails.get(1).getCommentInfoForMember().getIsLiked()).isFalse();
        assertThat(commentDetails.get(1).getCommentInfoForMember().getIsDisliked()).isTrue();
        assertThat(commentDetails.get(0).getCommentInfoForMember().getIsLiked()).isFalse();
        assertThat(commentDetails.get(0).getCommentInfoForMember().getIsDisliked()).isFalse();
    }

    @Test
    @DisplayName("좋아요 순 정렬 시 페이지네이션이 올바르게 동작한다")
    void getCommentDetails_LikeCountDescPagination() {
        // given
        for (int i = 0; i < 25; i++) {
            Comment comment = commentRepository.save(new Comment(member, bin, "댓글" + i));
            for (int j = 0; j < i / 5; j++) {
                comment.increaseLikeCount();
            }
            commentRepository.save(comment);
        }

        // when
        List<CommentDetail> firstPage = commentService.getCommentDetails(member.getEmail(), bin.getId(),
                CommentSort.LIKE_COUNT_DESC, null, null);
        CommentDetail lastCommentOfFirstPage = firstPage.get(firstPage.size() - 1);
        List<CommentDetail> secondPage = commentService.getCommentDetails(member.getEmail(), bin.getId(),
                CommentSort.LIKE_COUNT_DESC, lastCommentOfFirstPage.getCommentId(),
                lastCommentOfFirstPage.getLikeCount());

        // then
        assertThat(firstPage).hasSize(20);
        assertThat(secondPage).hasSize(5);

        // 정렬 검증
        Comparator<CommentDetail> commentDetailComparator = (c1, c2) -> {
            int likeCountCompare = c2.getLikeCount().compareTo(c1.getLikeCount());
            return likeCountCompare != 0 ? likeCountCompare : c2.getCommentId().compareTo(c1.getCommentId());
        };

        assertThat(firstPage).isSortedAccordingTo(commentDetailComparator);
        assertThat(secondPage).isSortedAccordingTo(commentDetailComparator);

        // 페이지네이션 연속성 검증
        assertThat(commentDetailComparator.compare(firstPage.get(firstPage.size() - 1), secondPage.get(0)))
                .isLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("댓글 목록을 조회할 때 삭제된 댓글은 포함되지 않는다.")
    void getCommentDetails_notContainsDeleted() {
        // given
        Comment comment1 = commentRepository.save(new Comment(member, bin, "댓글1"));
        Comment comment2 = commentRepository.save(new Comment(member, bin, "댓글2"));
        Comment comment3 = commentRepository.save(new Comment(member, bin, "댓글3"));

        commentService.deleteComment(member.getEmail(), comment1.getId());

        // when
        List<CommentDetail> commentDetails = commentService.getCommentDetails(member.getEmail(), bin.getId(),
                CommentSort.CREATED_AT_DESC, null, null);

        // then
        assertThat(commentDetails).hasSize(2);
        assertThat(commentDetails).extracting(CommentDetail::getCommentId)
                .containsExactly(comment3.getId(), comment2.getId());
    }

    @Test
    @DisplayName("싫어요를 누르면 싫어요 수가 1 증가한다.")
    void createCommentDislike_success() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글1"));
        commentRepository.save(comment);

        //when
        commentService.createCommentDislike(member.getEmail(), comment.getId());

        //then
        List<CommentDislike> all = commentDislikeRepository.findAll();
        assertThat(comment.getDislikeCount()).isEqualTo(1);
        assertThat(all.size()).isEqualTo(1);
        assertThat(all).extracting(commentDislike -> commentDislike.getMember().getId())
                .containsExactly(member.getId());
        assertThat(all).extracting(commentDislike -> commentDislike.getComment().getId())
                .containsExactly(comment.getId());
    }

    @Test
    @DisplayName("이미 싫어요를 누른 상태에서 싫어요를 누르면 예외가 발생한다.")
    void createCommentDislike_fail_isAlreadyDisliked() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글1"));
        commentRepository.save(comment);
        commentService.createCommentDislike(member.getEmail(), comment.getId());

        //when & then
        assertThatThrownBy(() -> commentService.createCommentDislike(member.getEmail(), comment.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("이미 좋아요를 누른 상태에서 싫어요를 누르면 좋아요가 1 감소하고 싫어요 수가 1 증가한다.")
    void createCommentDislike_success_isAlreadyLiked() {
        //given

        Comment comment = commentRepository.save(new Comment(member, bin, "댓글1"));
        commentRepository.save(comment);
        commentLikeRepository.save(new CommentLike(member, comment));
        comment.increaseLikeCount();

        assertThat(commentLikeRepository.findAll()).size().isEqualTo(1);
        assertThat(comment.getLikeCount()).isEqualTo(1);

        //when
        commentService.createCommentDislike(member.getEmail(), comment.getId());

        //then
        List<CommentDislike> commentDislikes = commentDislikeRepository.findAll();
        List<CommentLike> commentLikes = commentLikeRepository.findAll();

        assertThat(comment.getDislikeCount()).isEqualTo(1);
        assertThat(commentDislikes.size()).isEqualTo(1);
        assertThat(commentDislikes).extracting(commentDislike -> commentDislike.getMember().getId())
                .containsExactly(member.getId());
        assertThat(commentDislikes).extracting(commentDislike -> commentDislike.getComment().getId())
                .containsExactly(comment.getId());
        assertThat(commentLikes).size().isEqualTo(0);
        assertThat(comment.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("100명이 동시에 좋아요를 누르면 좋아요 수가 100 증가한다.")
    void createCommentLike_success_concurrent() throws InterruptedException, ExecutionException {
        //given
        int count = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(count);
        CountDownLatch latch = new CountDownLatch(count);

        transactionTemplate.execute((status) -> {
            deleteAll(); // 혹시 존재할 수 있는 데이터 삭제
            return null;
        });

        // 멤버 생성
        Member member = transactionTemplate.execute((status) -> {
            return memberRepository.save(new Member("test@email.com", "test", Role.ROLE_USER, null));
        });

        // 쓰레기통 생성
        Bin bin = transactionTemplate.execute((status) -> {

            Bin temp = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 11d), "address1", 0L, 0L, 0L, null,
                    null);
            return binRepository.save(temp);
        });

        // 코멘트 생성
        Comment comment = transactionTemplate.execute(
                (status) -> commentRepository.save(new Comment(member, bin, "댓글")));

        //when
        for (int i = 0; i < count; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    Member saved = memberRepository.save(
                            new Member("user" + finalI + "@email.com", "user" + finalI, Role.ROLE_USER, null));

                    commentService.createCommentLike(saved.getEmail(), comment.getId());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Comment updated = commentRepository.findById(comment.getId()).get();
        assertThat(updated.getLikeCount()).isEqualTo(count);
        assertThat(updated.getDislikeCount()).isEqualTo(0);

        List<CommentLike> commentLikes = commentLikeRepository.findAll();
        List<CommentDislike> commentDislikes = commentDislikeRepository.findAll();
        assertThat(commentLikes).size().isEqualTo(count);
        assertThat(commentDislikes).size().isEqualTo(0);

        executorService.submit(this::deleteAll).get(); // 메인 트랜잭션에서 데이터 삭제 하면 다시 롤백되므로 다른 트랜잭션에서 삭제
    }


    @Test
    @DisplayName("좋아요를 누르면 좋아요 수가 1 증가한다.")
    void createCommentLike_success() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글1"));
        commentRepository.save(comment);

        //when
        commentService.createCommentLike(member.getEmail(), comment.getId());

        //then
        List<CommentLike> all = commentLikeRepository.findAll();
        assertThat(comment.getLikeCount()).isEqualTo(1);
        assertThat(all.size()).isEqualTo(1);
        assertThat(all).extracting(commentLike -> commentLike.getMember().getId()).containsExactly(member.getId());
        assertThat(all).extracting(commentLike -> commentLike.getComment().getId()).containsExactly(comment.getId());
    }

    @Test
    @DisplayName("이미 좋아요를 누른 상태에서 좋아요를 누르면 예외가 발생한다.")
    void createCommentLike_fail_isAlreadyLiked() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글1"));
        commentRepository.save(comment);
        commentService.createCommentLike(member.getEmail(), comment.getId());

        //when & then
        assertThatThrownBy(() -> commentService.createCommentLike(member.getEmail(), comment.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("이미 싫어요를 누른 상태에서 좋아요를 누르면 싫어요가 1 감소하고 좋아요 수가 1 증가한다.")
    void createCommentLike_success_isAlreadyDisliked() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글1"));
        commentRepository.save(comment);
        commentDislikeRepository.save(new CommentDislike(member, comment));
        comment.increaseDislikeCount();

        assertThat(commentDislikeRepository.findAll()).size().isEqualTo(1);
        assertThat(comment.getDislikeCount()).isEqualTo(1);

        //when
        commentService.createCommentLike(member.getEmail(), comment.getId());

        //then
        List<CommentLike> commentLikes = commentLikeRepository.findAll();
        List<CommentDislike> commentDislikes = commentDislikeRepository.findAll();

        assertThat(comment.getLikeCount()).isEqualTo(1);
        assertThat(commentLikes.size()).isEqualTo(1);
        assertThat(commentLikes).extracting(commentLike -> commentLike.getMember().getId())
                .containsExactly(member.getId());
        assertThat(commentLikes).extracting(commentLike -> commentLike.getComment().getId())
                .containsExactly(comment.getId());
        assertThat(commentDislikes).size().isEqualTo(0);
        assertThat(comment.getDislikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("100명이 동시에 싫어요를 누르면 싫어요 수가 100 증가한다.")
    void createCommentDislike_success_concurrent() throws InterruptedException, ExecutionException {
        //given
        int count = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(count);
        CountDownLatch latch = new CountDownLatch(count);

        transactionTemplate.execute((status) -> {
            deleteAll(); // 혹시 존재할 수 있는 데이터 삭제
            return null;
        });

        // 멤버 생성
        Member member = transactionTemplate.execute((status) -> {
            return memberRepository.save(new Member("test@email.com", "test", Role.ROLE_USER, null));
        });

        // 쓰레기통 생성
        Bin bin = transactionTemplate.execute((status) -> {

            Bin temp = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 11d), "address1", 0L, 0L, 0L, null,
                    null);
            return binRepository.save(temp);
        });

        // 코멘트 생성
        Comment comment = transactionTemplate.execute(
                (status) -> commentRepository.save(new Comment(member, bin, "댓글")));

        //when
        for (int i = 0; i < count; i++) {
            int finalI = i;
            executorService.submit(() -> {
                try {
                    Member saved = memberRepository.save(
                            new Member("user" + finalI + "@email.com", "user" + finalI, Role.ROLE_USER, null));

                    commentService.createCommentDislike(saved.getEmail(), comment.getId());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Comment updated = commentRepository.findById(comment.getId()).get();
        assertThat(updated.getDislikeCount()).isEqualTo(count);
        assertThat(updated.getLikeCount()).isEqualTo(0);

        List<CommentLike> commentLikes = commentLikeRepository.findAll();
        List<CommentDislike> commentDislikes = commentDislikeRepository.findAll();
        assertThat(commentLikes).size().isEqualTo(0);
        assertThat(commentDislikes).size().isEqualTo(count);

        executorService.submit(this::deleteAll).get(); // 메인 트랜잭션에서 데이터 삭제 하면 다시 롤백되므로 다른 트랜잭션에서 삭제
    }

    @Test
    @DisplayName("좋아요한 내역이 있으면 취소가 가능하다.")
    void deleteCommentLike_success() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));
        commentService.createCommentLike(member.getEmail(), comment.getId());

        //when
        commentService.deleteCommentLike(member.getEmail(), comment.getId());

        //then
        assertThat(comment.getLikeCount()).isEqualTo(0);
        assertThat(commentLikeRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("싫어요한 내역이 있으면 취소가 가능하다.")
    void deleteCommentDislike_success() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));
        commentService.createCommentDislike(member.getEmail(), comment.getId());

        //when
        commentService.deleteCommentDislike(member.getEmail(), comment.getId());

        //then
        assertThat(comment.getDislikeCount()).isEqualTo(0);
        assertThat(commentDislikeRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요한 내역이 없으면 취소가 불가능하다.")
    void deleteCommentLike_fail() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when & then
        assertThatThrownBy(() -> commentService.deleteCommentLike(member.getEmail(), comment.getId()))
                .isInstanceOf(BadRequestException.class);

    }

    @Test
    @DisplayName("싫어요한 내역이 없으면 취소가 불가능하다.")
    void deleteCommentDislike_fail() {
        //given
        Comment comment = commentRepository.save(new Comment(member, bin, "댓글"));

        //when & then
        assertThatThrownBy(() -> commentService.deleteCommentDislike(member.getEmail(), comment.getId()))
                .isInstanceOf(BadRequestException.class);

    }

    @Test
    @DisplayName("댓글 내용에 욕설이 포함되어 있으면 예외가 발생한다.")
    void createComment_fail_isCurse() {
        //when & then
        assertThatThrownBy(() -> commentService.createComment(member.getEmail(), bin.getId()
                , "시1발 쓰레기통 위치가 안 맞잖아"))
                .isInstanceOf(BadRequestException.class);

    }

    private void deleteAll() {
        commentLikeRepository.deleteAll();
        commentDislikeRepository.deleteAll();
        commentRepository.deleteAll();
        binRepository.deleteAll();
        memberRepository.deleteAll();
    }
}
