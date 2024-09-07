package net.binder.api.binmodification.repository;

import net.binder.api.binmodification.entity.BinModification;
import net.binder.api.binmodification.entity.BinModificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinModificationRepository extends JpaRepository<BinModification, Long> {
    Long countByStatus(BinModificationStatus status);

    boolean existsByBinIdAndStatus(Long id, BinModificationStatus binModificationStatus);
}
