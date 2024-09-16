package net.binder.api.admin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.dto.BinUpdateRequest;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.service.NotificationService;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBinManagementService {

    private final BinRepository binRepository;

    private final MemberService memberService;

    private final NotificationService notificationService;

    public void updateBin(String email, Long binId, BinUpdateRequest request) {
        Member admin = memberService.findByEmail(email);

        Bin bin = getBinOrThrow(binId);
        Point newPoint = PointUtil.getPoint(request.getLongitude(), request.getLatitude());

        bin.update(request.getTitle(), request.getType(), newPoint, request.getAddress(), request.getImageUrl());

        notificationService.sendNotification(admin, getReceiver(bin), bin, NotificationType.BIN_MODIFIED,
                request.getModificationReason());
    }

    public void deleteBin(String email, Long binId, String deleteReason) {
        Member admin = memberService.findByEmail(email);

        Bin bin = getBinOrThrow(binId);

        boolean isDeleted = bin.softDelete();

        if (!isDeleted) {
            throw new BadRequestException("이미 삭제 처리된 쓰레기통입니다.");
        }

        notificationService.sendNotification(admin, getReceiver(bin), bin, NotificationType.BIN_DELETED, deleteReason);
    }

    private Bin getBinOrThrow(Long binId) {
        return binRepository.findById(binId)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 쓰레기통입니다."));
    }

    private Member getReceiver(Bin bin) {
        BinRegistration binRegistration = bin.getBinRegistration();
        if (binRegistration == null) {
            return null;
        }

        return binRegistration.getMember();
    }
}
