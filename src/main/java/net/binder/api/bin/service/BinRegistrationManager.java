package net.binder.api.bin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
@Transactional
public class BinRegistrationManager {

    public void approve(BinRegistration binRegistration) {
        validateRegistrationStatus(binRegistration);

        binRegistration.approve();
    }

    public void reject(BinRegistration binRegistration) {
        validateRegistrationStatus(binRegistration);

        binRegistration.reject();
    }

    private void validateRegistrationStatus(BinRegistration binRegistration) {
        if (!binRegistration.isPending()) {
            throw new BadRequestException("이미 심사가 완료된 상태입니다.");
        }
    }
}
