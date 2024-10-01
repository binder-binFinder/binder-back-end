package net.binder.api.bin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinReader {

    private final BinRepository binRepository;

    public Bin readOne(Long binId) {
        return binRepository.findById(binId)
                .orElseThrow(() -> new BadRequestException(String.format("존재하지 않는 쓰레기통입니다. binId = %d", binId)));
    }
}
