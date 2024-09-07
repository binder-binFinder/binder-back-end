package net.binder.api.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinModificationDetail;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.admin.repository.AdminBinModificationQueryRepository;
import net.binder.api.binmodification.entity.BinModification;
import net.binder.api.binmodification.entity.BinModificationStatus;
import net.binder.api.binmodification.repository.BinModificationRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
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

    private final AdminBinModificationQueryRepository adminBinModificationQueryRepository;

    private final BinModificationRepository binModificationRepository;
    private final MemberService memberService;

    @Transactional(readOnly = true)
    public List<BinModificationDetail> getBinModificationDetails(ModificationFilter filter) {
        List<BinModification> binModifications = adminBinModificationQueryRepository.findAll(filter);

        return binModifications.stream()
                .map(BinModificationDetail::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getModificationPendingCount() {
        return binModificationRepository.countByStatus(BinModificationStatus.PENDING);
    }

    public void approveModification(String email, Long id) {
        Member admin = memberService.findByEmail(email);

        BinModification binModification = findModificationOrThrow(id);

        validateModificationStatus(binModification);

        binModification.approve();

        notificationService.sendNotification(admin, binModification.getMember(), binModification.getBin(),
                NotificationType.BIN_MODIFICATION_APPROVED, null);
    }

    public void rejectModification(String email, Long id, String rejectReason) {
        Member admin = memberService.findByEmail(email);
        BinModification binModification = findModificationOrThrow(id);

        validateModificationStatus(binModification);

        binModification.reject();

        notificationService.sendNotification(admin, binModification.getMember(), binModification.getBin(),
                NotificationType.BIN_MODIFICATION_REJECTED, rejectReason);
    }

    private BinModification findModificationOrThrow(Long id) {
        return binModificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 쓰레기통 수정 요청입니다."));
    }

    private void validateModificationStatus(BinModification binModification) {
        if (!binModification.isPending()) {
            throw new BadRequestException("이미 심사가 완료된 상태입니다.");
        }
    }
}
