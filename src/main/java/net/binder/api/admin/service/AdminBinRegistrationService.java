package net.binder.api.admin.service;

import static net.binder.api.binregistration.entity.BinRegistrationStatus.PENDING;
import static net.binder.api.notification.entity.NotificationType.BIN_REGISTRATION_APPROVED;
import static net.binder.api.notification.entity.NotificationType.BIN_REGISTRATION_REJECTED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinRegistrationDetail;
import net.binder.api.admin.dto.RegistrationFilter;
import net.binder.api.admin.repository.BinRegistrationQueryRepository;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminBinRegistrationService {

    private final BinRegistrationRepository binRegistrationRepository;

    private final BinRegistrationQueryRepository binRegistrationQueryRepository;

    private final NotificationService notificationService;

    public void approveRegistration(Long id) {
        BinRegistration binRegistration = findRegistrationOrThrow(id);

        validateRegistrationStatus(binRegistration);

        binRegistration.approve();

        notificationService.sendNotification(binRegistration.getMember(), binRegistration.getBin(),
                BIN_REGISTRATION_APPROVED, null);
    }

    public void rejectRegistration(Long id, String rejectReason) {
        BinRegistration binRegistration = findRegistrationOrThrow(id);

        validateRegistrationStatus(binRegistration);

        binRegistration.reject();

        notificationService.sendNotification(binRegistration.getMember(), binRegistration.getBin(),
                BIN_REGISTRATION_REJECTED, rejectReason);
    }

    @Transactional(readOnly = true)
    public List<BinRegistrationDetail> getBinRegistrationDetails(RegistrationFilter sort) {
        List<BinRegistration> binRegistrations = binRegistrationQueryRepository.findAll(sort);

        return binRegistrations.stream()
                .map(BinRegistrationDetail::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getRegistrationPendingCount() {
        return binRegistrationRepository.countByStatus(PENDING);
    }

    private BinRegistration findRegistrationOrThrow(Long id) {
        return binRegistrationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 쓰레기통 등록 요청입니다."));
    }

    private void validateRegistrationStatus(BinRegistration binRegistration) {
        if (!binRegistration.isPending()) {
            throw new BadRequestException("이미 심사가 완료된 상태입니다.");
        }
    }
}
