package net.binder.api.likeanddislike.service;

import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.entity.BinRegistrationStatus;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.likeanddislike.entity.MemberDislikeBin;
import net.binder.api.likeanddislike.entity.MemberLikeBin;
import net.binder.api.likeanddislike.repository.MemberDislikeBinRepository;
import net.binder.api.likeanddislike.repository.MemberLikeBinRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class LikeAndDislikeFacadeTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private MemberLikeBinService memberLikeBinService;

    @Autowired
    private MemberLikeBinRepository memberLikeBinRepository;

    @Autowired
    private MemberDislikeBinRepository memberDislikeBinRepository;

    @Autowired
    private LikeAndDislikeFacade likeAndDislikeFacade;

    @Autowired
    private NotificationRepository notificationRepository;

    private Member testMember;

    private Member testMember2;

    private Bin bin1;

    private Bin bin2;

    private Bin bin3;

    private Bin bin4;

    @BeforeEach
    void setUp() {
        testMember = new Member("dusgh7031@gmail.com", "테스트", Role.ROLE_USER, "http://example.com/image.jpg");
        testMember2 = new Member("dusgh70312@gmail.com", "테스트2", Role.ROLE_USER, "http://example.com/image.jpg");
        memberRepository.save(testMember);
        memberRepository.save(testMember2);

        bin1 = new Bin("title1", BinType.CIGAR, PointUtil.getPoint(126.971969841012, 37.578567094578), "address1", 0L, 0L, 0L, null, null);
        bin2 = new Bin("title2", BinType.GENERAL, PointUtil.getPoint(126.971968136353, 37.577376610574), "address2", 0L, 0L, 0L, null, null);
        bin3 = new Bin("title3", BinType.BEVERAGE, PointUtil.getPoint(126.971968136353, 37.577376610574), "address3", 0L, 0L, 0L, null, null);
        bin4 = new Bin("title4", BinType.BEVERAGE, PointUtil.getPoint(126.97154998287, 37.579971733838), "address4", 0L, 0L, 0L, null, null);

        binRepository.save(bin1);
        binRepository.save(bin2);
        binRepository.save(bin3);
        binRepository.save(bin4);
    }


    @Test
    @DisplayName("좋아요를 누르면 카운트가 1 증가하고 알림이 전송된다.")
    void createLike() {
        //given
        Member creator = new Member("creator@email.com", "creator", Role.ROLE_USER, null);
        Member likeMaker = new Member("likeMaker@email.com", "likeMaker", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(creator, likeMaker));

        Bin bin = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(creator, null, BinRegistrationStatus.APPROVED);
        bin.setBinRegistration(binRegistration);
        binRepository.save(bin);

        //when
        likeAndDislikeFacade.createLike(likeMaker.getEmail(), bin.getId());
        List<MemberLikeBin> memberLikeBins = memberLikeBinRepository.findAll();
        List<Notification> notifications = notificationRepository.findAll();

        //then
        assertThat(bin.getLikeCount()).isEqualTo(1);
        assertThat(memberLikeBins.size()).isEqualTo(1);

        MemberLikeBin memberLikeBin = memberLikeBins.get(0);
        assertThat(memberLikeBin.getBin()).isEqualTo(bin);
        assertThat(memberLikeBin.getMember()).isEqualTo(likeMaker);

        assertThat(notifications.size()).isEqualTo(1);
        Notification notification = notifications.get(0);
        assertThat(notification.getSender()).isEqualTo(likeMaker);
        assertThat(notification.getReceiver()).isEqualTo(creator);
        assertThat(notification.getType()).isEqualTo(NotificationType.BIN_LIKED);
    }

    @Test
    @DisplayName("좋아요를 취소하고 다시 좋아요를 누르면 알림이 전송되지 않는다.")
    void createLike_hasNotification() {
        //given
        Member creator = new Member("creator@email.com", "creator", Role.ROLE_USER, null);
        Member likeMaker = new Member("likeMaker@email.com", "likeMaker", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(creator, likeMaker));

        Bin bin = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(creator, null, BinRegistrationStatus.APPROVED);
        bin.setBinRegistration(binRegistration);
        binRepository.save(bin);

        likeAndDislikeFacade.createLike(likeMaker.getEmail(), bin.getId());
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        assertThat(memberLikeBinRepository.findAll().size()).isEqualTo(1);

        memberLikeBinService.deleteLike(likeMaker.getId(), bin.getId());
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        assertThat(memberLikeBinRepository.findAll().size()).isEqualTo(0);

        //when
        memberLikeBinService.createLike(likeMaker, bin.getId());

        //then
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("싫어요를 누르면 쓰레기통의 싫어요 카운트가 1 올라간다.")
    void createDisLike() {

        //when
        likeAndDislikeFacade.createDislike(testMember.getEmail(), bin1.getId());
        List<MemberDislikeBin> dislikes = memberDislikeBinRepository.findAll();

        Bin find = binRepository.findById(bin1.getId()).get();

        //then
        assertThat(find.getDislikeCount()).isEqualTo(1);
        assertThat(dislikes.size()).isEqualTo(1);

        MemberDislikeBin memberDislikeBin = dislikes.get(0);
        assertThat(memberDislikeBin.getBin().getId()).isEqualTo(bin1.getId());
        assertThat(memberDislikeBin.getMember().getId()).isEqualTo(testMember.getId());
    }


    @Test
    @DisplayName("싫어요를 취소하면 쓰레기통의 싫어요 카운트가 1 내려간다")
    void deleteDisLike() {

        //when
        likeAndDislikeFacade.createDislike(testMember.getEmail(), bin1.getId());

        likeAndDislikeFacade.deleteDislike(testMember.getEmail(), bin1.getId());

        Bin find = binRepository.findById(bin1.getId()).get();
        List<MemberDislikeBin> dislikes = memberDislikeBinRepository.findAll();


        //then
        assertThat(find.getDislikeCount()).isEqualTo(0);
        assertThat(dislikes.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요를 중복해서 누를 수 없다.")
    void no_duplicate_Like() {

        //when
        likeAndDislikeFacade.createLike(testMember.getEmail(), bin1.getId());

        assertThatThrownBy(()->likeAndDislikeFacade.createLike(testMember.getEmail(), bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 좋아요를 누른 쓰레기통입니다.");

        Bin bin = binRepository.findById(bin1.getId()).get();
        assertThat(bin.getLikeCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("싫어요를 중복해서 누를 수 없다.")
    void no_duplicate_Dislike() {

        likeAndDislikeFacade.createDislike(testMember.getEmail(), bin1.getId());

        assertThatThrownBy(()->likeAndDislikeFacade.createDislike(testMember.getEmail(), bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 좋아요를 누른 쓰레기통입니다.");

        Bin bin = binRepository.findById(bin1.getId()).get();
        assertThat(bin.getDislikeCount()).isEqualTo(1L);
    }


    @Test
    @DisplayName("좋아요를 누르지 않은 쓰레기통의 좋아요 취소를 할 수 없다.")
    void no_delete_like_bin_that_is_not_liked() {

        assertThatThrownBy(()->likeAndDislikeFacade.deleteLike(testMember.getEmail(), bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("좋아요를 누르지 않았던 쓰레기통입니다.");
    }

    @Test
    @DisplayName("싫어요를 누르지 않은 쓰레기통의 싫어요 취소를 할 수 없다.")
    void no_delete_dislike_bin_that_is_not_disliked() {

        assertThatThrownBy(()->likeAndDislikeFacade.deleteDislike(testMember.getEmail(), bin1.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("싫어요를 누르지 않았던 쓰레기통입니다.");
    }

    @Test
    @DisplayName("좋아요를 누르고 싫어요를 누르면 좋아요가 취소된다.")
    void like_after_dislike() {

        //when
        likeAndDislikeFacade.createLike(testMember.getEmail(), bin1.getId());
        List<MemberLikeBin> likes = memberLikeBinRepository.findAll();

        Bin find = binRepository.findById(bin1.getId()).get();

        //then
        assertThat(find.getLikeCount()).isEqualTo(1);
        assertThat(likes.size()).isEqualTo(1);

        likeAndDislikeFacade.createDislike(testMember.getEmail(), bin1.getId());
        List<MemberLikeBin> likes2 = memberLikeBinRepository.findAll();
        List<MemberDislikeBin> dislikes = memberDislikeBinRepository.findAll();
        Bin find2 = binRepository.findById(bin1.getId()).get();

        assertThat(find2.getLikeCount()).isEqualTo(0);
        assertThat(find2.getDislikeCount()).isEqualTo(1);
        assertThat(likes2.size()).isEqualTo(0);
        assertThat(dislikes.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("싫어요를 누르고 좋아요를 누르면 싫어요가 취소된다.")
    void dislike_after_like() {

        //when
        likeAndDislikeFacade.createDislike(testMember.getEmail(), bin1.getId());
        List<MemberDislikeBin> dislikes = memberDislikeBinRepository.findAll();

        Bin find = binRepository.findById(bin1.getId()).get();

        //then
        assertThat(find.getDislikeCount()).isEqualTo(1);
        assertThat(dislikes.size()).isEqualTo(1);

        likeAndDislikeFacade.createLike(testMember.getEmail(), bin1.getId());
        List<MemberDislikeBin> dislikes2 = memberDislikeBinRepository.findAll();
        List<MemberLikeBin> likes = memberLikeBinRepository.findAll();
        Bin find2 = binRepository.findById(bin1.getId()).get();

        assertThat(find2.getLikeCount()).isEqualTo(1);
        assertThat(find2.getDislikeCount()).isEqualTo(0);
        assertThat(dislikes2.size()).isEqualTo(0);
        assertThat(likes.size()).isEqualTo(1);
    }

}