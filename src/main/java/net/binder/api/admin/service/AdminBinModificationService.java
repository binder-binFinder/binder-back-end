package net.binder.api.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinModificationDetail;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.admin.repository.BinModificationQueryRepository;
import net.binder.api.binmodification.entity.BinModification;
import net.binder.api.binmodification.entity.BinModificationStatus;
import net.binder.api.binmodification.repository.BinModificationRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBinModificationService {

    private final NotificationService notificationService;

    private final BinModificationQueryRepository binModificationQueryRepository;

    private final BinModificationRepository binModificationRepository;

//    public void approveModification(Long id) {
//        BinRegistration binRegistration = findRegistrationOrThrow(id);
//
//        validateRegistrationStatus(binRegistration);
//
//        binRegistration.approve();
//
//        notificationService.sendNotification(binRegistration.getMember(), binRegistration.getBin(),
//                BIN_REGISTRATION_APPROVED, null);
//    }
//
//    public void rejectRegistration(Long id, String rejectReason) {
//        BinRegistration binRegistration = findRegistrationOrThrow(id);
//
//        validateRegistrationStatus(binRegistration);
//
//        binRegistration.reject();
//
//        notificationService.sendNotification(binRegistration.getMember(), binRegistration.getBin(),
//                BIN_REGISTRATION_REJECTED, rejectReason);
//    }

    @Transactional(readOnly = true)
    public List<BinModificationDetail> getBinModificationDetails(ModificationFilter filter) {
        List<BinModification> binModifications = binModificationQueryRepository.findAll(filter);

        return binModifications.stream()
                .map(BinModificationDetail::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getModificationPendingCount() {
        return binModificationRepository.countByStatus(BinModificationStatus.PENDING);
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
