package net.binder.api.bin.repository;

import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.entity.BinModificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinModificationRepository extends JpaRepository<BinModification, Long> {
    Long countByStatus(BinModificationStatus status);

    boolean existsByBinIdAndStatus(Long id, BinModificationStatus binModificationStatus);
}
