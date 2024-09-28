package net.binder.api.bin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.repository.BinQueryRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BinRegistrationReader {

    private final BinQueryRepository binQueryRepository;

    @Transactional(readOnly = true)
    public List<BinRegistration> readAll(Long memberId, Long lastBinId, int pageSize) {

        return binQueryRepository.findAll(memberId, lastBinId, pageSize);
    }
}
