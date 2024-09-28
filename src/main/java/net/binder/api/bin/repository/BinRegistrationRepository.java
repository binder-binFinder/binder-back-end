package net.binder.api.bin.repository;

import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.entity.BinRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinRegistrationRepository extends JpaRepository<BinRegistration, Long> {

    Long countByStatus(BinRegistrationStatus status);
}
