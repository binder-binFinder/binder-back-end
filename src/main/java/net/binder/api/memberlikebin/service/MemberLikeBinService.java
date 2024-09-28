package net.binder.api.memberlikebin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.memberlikebin.entity.MemberLikeBin;
import net.binder.api.memberlikebin.repository.MemberLikeBinRepository;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberLikeBinService {

    private final MemberLikeBinRepository memberLikeBinRepository;
    private final MemberService memberService;
    private final BinService binService;
    private final NotificationService notificationService;

    public void saveLike(String email, Long binId) {
        Member sender = memberService.findByEmail(email);

        if (memberLikeBinRepository.existsByMember_IdAndBin_Id(sender.getId(), binId)) {
            throw new BadRequestException("이미 좋아요를 누른 쓰레기통입니다.");
        }

        Bin bin = binService.findById(binId);

        bin.increaseLike();
        MemberLikeBin memberLikeBin = MemberLikeBin.builder()
                .member(sender)
                .bin(bin)
                .build();
        memberLikeBinRepository.save(memberLikeBin);

        if (!notificationService.hasLikeNotification(sender, bin)) {
            notificationService.sendNotification(sender, getReceiver(bin), bin, NotificationType.BIN_LIKED, null);
        }
    }

    public void deleteLike(String email, Long binId) {
        Member member = memberService.findByEmail(email);

        if (!memberLikeBinRepository.existsByMember_IdAndBin_Id(member.getId(), binId)) {
            throw new BadRequestException("좋아요를 누르지 않았던 쓰레기통입니다.");
        }
        Bin bin = binService.findById(binId);
        bin.decreaseLike();
        memberLikeBinRepository.deleteMemberLikeBinByMember_EmailAndBin_Id(email, binId);
    }

    private Member getReceiver(Bin bin) {
        BinRegistration binRegistration = bin.getBinRegistration();
        if (binRegistration == null) {
            return null;
        }
        return binRegistration.getMember();
    }
}
