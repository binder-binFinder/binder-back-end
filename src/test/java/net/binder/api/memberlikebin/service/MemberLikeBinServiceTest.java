package net.binder.api.memberlikebin.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.memberlikebin.entity.MemberLikeBin;
import net.binder.api.memberlikebin.repository.MemberLikeBinRepository;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberLikeBinServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private MemberLikeBinService memberLikeBinService;

    @Autowired
    private MemberLikeBinRepository memberLikeBinRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("좋아요를 누르면 카운트가 1 증가하고 알림이 전송된다.")
    void saveLike() {
        //given
        Member creator = new Member("creator@email.com", "creator", Role.ROLE_USER, null);
        Member likeMaker = new Member("likeMaker@email.com", "likeMaker", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(creator, likeMaker));

        Bin bin = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(creator, null, BinRegistrationStatus.APPROVED);
        bin.setBinRegistration(binRegistration);
        binRepository.save(bin);

        //when
        memberLikeBinService.saveLike(likeMaker.getEmail(), bin.getId());
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
    void saveLike_hasNotification() {
        //given
        Member creator = new Member("creator@email.com", "creator", Role.ROLE_USER, null);
        Member likeMaker = new Member("likeMaker@email.com", "likeMaker", Role.ROLE_USER, null);
        memberRepository.saveAll(List.of(creator, likeMaker));

        Bin bin = new Bin("title", BinType.GENERAL, PointUtil.getPoint(100d, 10d), "address", 0L, 0L, 0L, null, null);
        BinRegistration binRegistration = new BinRegistration(creator, null, BinRegistrationStatus.APPROVED);
        bin.setBinRegistration(binRegistration);
        binRepository.save(bin);

        memberLikeBinService.saveLike(likeMaker.getEmail(), bin.getId());
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        assertThat(memberLikeBinRepository.findAll().size()).isEqualTo(1);

        memberLikeBinService.deleteLike(likeMaker.getEmail(), bin.getId());
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
        assertThat(memberLikeBinRepository.findAll().size()).isEqualTo(0);

        //when
        memberLikeBinService.saveLike(likeMaker.getEmail(), bin.getId());

        //then
        assertThat(notificationRepository.findAll().size()).isEqualTo(1);
    }
}