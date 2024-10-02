package net.binder.api.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinModificationDetail;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.entity.BinModificationStatus;
import net.binder.api.bin.service.BinManager;
import net.binder.api.bin.service.BinModificationManager;
import net.binder.api.bin.service.BinModificationReader;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBinModificationService {

    private final NotificationService notificationService;

    private final BinModificationReader binModificationReader;

    private final BinModificationManager binModificationManager;

    private final BinManager binManager;

    private final MemberService memberService;

    @Transactional(readOnly = true)
    public List<BinModificationDetail> getBinModificationDetails(ModificationFilter filter) {
        List<BinModification> binModifications = binModificationReader.readAll(filter);

        return binModifications.stream()
                .map(BinModificationDetail::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getModificationPendingCount() {
        return binModificationReader.countByStatus(BinModificationStatus.PENDING);
    }

    public void approveModification(String email, Long modificationId) {
        Member admin = memberService.findByEmail(email);

        BinModification binModification = binModificationReader.readOne(modificationId);

        binManager.update(binModification.getBin(), binModification.getTitle(), binModification.getType()
                , binModification.getAddress(), binModification.getLongitude(), binModification.getLatitude(),
                binModification.getImageUrl());

        binModificationManager.approve(binModification);

        notificationService.sendNotification(admin, binModification.getMember(), binModification.getBin(),
                NotificationType.BIN_MODIFICATION_APPROVED, null);
    }

    public void rejectModification(String email, Long modificationId, String rejectReason) {
        Member admin = memberService.findByEmail(email);

        BinModification binModification = binModificationReader.readOne(modificationId);

        binModificationManager.reject(binModification);

        notificationService.sendNotification(admin, binModification.getMember(), binModification.getBin(),
                NotificationType.BIN_MODIFICATION_REJECTED, rejectReason);
    }

}
