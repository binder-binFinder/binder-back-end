package net.binder.api.bin.repository;

import net.binder.api.bin.entity.Bin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinRepository extends JpaRepository<Bin, Long> {
}
