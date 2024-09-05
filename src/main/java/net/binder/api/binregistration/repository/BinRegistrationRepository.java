package net.binder.api.binregistration.repository;

import net.binder.api.binregistration.entity.BinRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinRegistrationRepository extends JpaRepository<BinRegistration, Long> {
}
