package net.binder.api.bin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.entity.BinModificationStatus;
import net.binder.api.bin.repository.BinModificationRepository;
import net.binder.api.bin.repository.BinQueryRepository;
import net.binder.api.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BinModificationReader {

    private final BinModificationRepository binModificationRepository;

    private final BinQueryRepository binQueryRepository;

    public BinModification readOne(Long modificationId) {
        return binModificationRepository.findById(modificationId)
                .orElseThrow(() -> new BadRequestException(
                        String.format("존재하지 않는 수정 요청입니다. modificationId = %d", modificationId)));
    }

    public List<BinModification> readAll(ModificationFilter filter) {
        return binQueryRepository.findAll(filter);
    }

    public Long countByStatus(BinModificationStatus binModificationStatus) {
        return binModificationRepository.countByStatus(binModificationStatus);
    }
}
