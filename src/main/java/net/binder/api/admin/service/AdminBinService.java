package net.binder.api.admin.service;

import static net.binder.api.notification.entity.NotificationType.BIN_REGISTRATION_APPROVED;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminBinService {

    private final BinRepository binRepository;

    private final NotificationService notificationService;

    public void approveRegistration(Long id) {
        Bin bin = findOrThrow(id);

        validateRegistrationStatus(bin);

        BinRegistration binRegistration = bin.getBinRegistration();
        binRegistration.approve();

        notificationService.sendNotification(binRegistration.getMember(), bin, BIN_REGISTRATION_APPROVED, null);
    }

    private Bin findOrThrow(Long id) {
        return binRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 쓰레기통입니다."));
    }

    private void validateRegistrationStatus(Bin bin) {
        if (!bin.isPending()) {
            throw new BadRequestException("이미 심사가 완료된 상태입니다.");
        }
    }
}
