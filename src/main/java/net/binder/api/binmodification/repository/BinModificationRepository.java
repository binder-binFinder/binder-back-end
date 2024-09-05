package net.binder.api.binmodification.repository;

import net.binder.api.binmodification.entity.BinModification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinModificationRepository extends JpaRepository<BinModification, Long> {
}
