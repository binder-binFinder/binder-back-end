package net.binder.api.admin.service;

import static net.binder.api.bin.entity.BinRegistrationStatus.PENDING;
import static net.binder.api.notification.entity.NotificationType.BIN_REGISTRATION_APPROVED;
import static net.binder.api.notification.entity.NotificationType.BIN_REGISTRATION_REJECTED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinRegistrationDetail;
import net.binder.api.admin.dto.RegistrationFilter;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.service.BinRegistrationManager;
import net.binder.api.bin.service.BinRegistrationReader;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminBinRegistrationService {

    private final MemberService memberService;

    private final BinRegistrationManager binRegistrationManager;

    private final BinRegistrationReader binRegistrationReader;

    private final NotificationService notificationService;

    public void approveRegistration(String email, Long registrationId) {
        Member admin = memberService.findByEmail(email);

        BinRegistration binRegistration = binRegistrationReader.readOne(registrationId);

        binRegistrationManager.approve(binRegistration);

        notificationService.sendNotification(admin, binRegistration.getMember(), binRegistration.getBin(),
                BIN_REGISTRATION_APPROVED, null);
    }

    public void rejectRegistration(String email, Long registrationId, String rejectReason) {
        Member admin = memberService.findByEmail(email);
        BinRegistration binRegistration = binRegistrationReader.readOne(registrationId);

        binRegistrationManager.reject(binRegistration);

        notificationService.sendNotification(admin, binRegistration.getMember(), binRegistration.getBin(),
                BIN_REGISTRATION_REJECTED, rejectReason);
    }

    @Transactional(readOnly = true)
    public List<BinRegistrationDetail> getBinRegistrationDetails(RegistrationFilter filter) {
        List<BinRegistration> binRegistrations = binRegistrationReader.readAll(filter);

        return binRegistrations.stream()
                .map(BinRegistrationDetail::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long getRegistrationPendingCount() {
        return binRegistrationReader.countByStatus(PENDING);
    }
}
