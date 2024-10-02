package net.binder.api.admin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.AdminBinUpdateRequest;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.service.BinManager;
import net.binder.api.bin.service.BinModificationManager;
import net.binder.api.bin.service.BinModificationReader;
import net.binder.api.bin.service.BinReader;
import net.binder.api.bin.service.BinRegistrationManager;
import net.binder.api.bin.service.BinRegistrationReader;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBinManagementService {

    private final BinRegistrationReader binRegistrationReader;

    private final BinRegistrationManager binRegistrationManager;

    private final BinModificationReader binModificationReader;

    private final BinModificationManager binModificationManager;

    private final BinReader binReader;

    private final BinManager binManager;

    private final MemberService memberService;

    private final NotificationService notificationService;

    public void updateBin(String email, Long binId, AdminBinUpdateRequest request) {
        Member admin = memberService.findByEmail(email);

        Bin bin = binReader.readOne(binId);

        binManager.update(bin, request.getTitle(), request.getType(), request.getAddress(), request.getLongitude(),
                request.getLatitude(), request.getImageUrl());

        notificationService.sendNotification(admin, getReceiver(bin), bin, NotificationType.BIN_MODIFIED,
                request.getModificationReason());

        approveRegistrationIfExists(request, admin);

        approveModificationIfExists(request, admin, bin);
    }

    public void deleteBin(String email, Long binId, String deleteReason) {
        Member admin = memberService.findByEmail(email);

        Bin bin = binReader.readOne(binId);

        boolean isDeleted = bin.softDelete();

        if (!isDeleted) {
            throw new BadRequestException("이미 삭제 처리된 쓰레기통입니다.");
        }

        notificationService.sendNotification(admin, getReceiver(bin), bin, NotificationType.BIN_DELETED, deleteReason);
    }

    private Member getReceiver(Bin bin) {
        BinRegistration binRegistration = bin.getBinRegistration();
        if (binRegistration == null) {
            return null;
        }

        return binRegistration.getMember();
    }

    private void approveRegistrationIfExists(AdminBinUpdateRequest request, Member admin) {
        if (request.getRegistrationId() != null) {
            BinRegistration binRegistration = binRegistrationReader.readOne(request.getRegistrationId());
            binRegistrationManager.approve(binRegistration);
            notificationService.sendNotification(admin, binRegistration.getMember(), binRegistration.getBin(),
                    NotificationType.BIN_REGISTRATION_APPROVED, null);
        }
    }

    private void approveModificationIfExists(AdminBinUpdateRequest request, Member admin, Bin bin) {
        if (request.getModificationId() != null) {
            BinModification binModification = binModificationReader.readOne(request.getModificationId());
            binModificationManager.approve(binModification);
            notificationService.sendNotification(admin, binModification.getMember(), bin,
                    NotificationType.BIN_MODIFICATION_APPROVED, null);
        }
    }
}
