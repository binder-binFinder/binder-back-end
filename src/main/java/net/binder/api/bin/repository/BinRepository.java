package net.binder.api.bin.repository;

import net.binder.api.bin.entity.Bin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface BinRepository extends JpaRepository<Bin, Long> {

    @Query("SELECT b FROM Bin b WHERE b.deletedAt IS NULL AND b.id = :id")
    Optional<Bin> findByIdAndNotDeleted(@Param("id") Long id);
}
