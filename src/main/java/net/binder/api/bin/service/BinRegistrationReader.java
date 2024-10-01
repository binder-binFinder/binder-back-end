package net.binder.api.bin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.RegistrationFilter;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.entity.BinRegistrationStatus;
import net.binder.api.bin.repository.BinQueryRepository;
import net.binder.api.bin.repository.BinRegistrationRepository;
import net.binder.api.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BinRegistrationReader {

    private final BinRegistrationRepository binRegistrationRepository;

    private final BinQueryRepository binQueryRepository;

    public List<BinRegistration> readAll(Long memberId, Long lastBinId, int pageSize) {

        return binQueryRepository.findAll(memberId, lastBinId, pageSize);
    }

    public BinRegistration readOne(Long registrationId) {
        return binRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new BadRequestException(
                        String.format("존재하지 않는 등록 요청입니다. registrationId = %d", registrationId)));
    }

    public List<BinRegistration> readAll(RegistrationFilter filter) {
        return binQueryRepository.findAll(filter);
    }

    public Long countByStatus(BinRegistrationStatus binRegistrationStatus) {
        return binRegistrationRepository.countByStatus(binRegistrationStatus);
    }
}
