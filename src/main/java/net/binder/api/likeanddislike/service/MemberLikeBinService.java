package net.binder.api.likeanddislike.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.likeanddislike.entity.MemberLikeBin;
import net.binder.api.likeanddislike.repository.MemberLikeBinRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberLikeBinService {

    private final MemberLikeBinRepository memberLikeBinRepository;
    private final BinService binService;
    private final NotificationService notificationService;

    public void createLike(Member sender, Long binId) {

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

    public void deleteLike(Long memberId, Long binId) {
        Bin bin = binService.findById(binId);
        bin.decreaseLike();
        memberLikeBinRepository.deleteMemberLikeBinByMember_IdAndBin_Id(memberId, binId);
    }

    public boolean isLikeExist(Long memberId, Long binId){
        return memberLikeBinRepository.existsByMember_IdAndBin_Id(memberId, binId);
    }

    private Member getReceiver(Bin bin) {
        BinRegistration binRegistration = bin.getBinRegistration();
        if (binRegistration == null) {
            return null;
        }
        return binRegistration.getMember();
    }
}
