package net.binder.api.filtering.repository;

import net.binder.api.filtering.entity.Curse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurseRepository extends JpaRepository<Curse, Long> {
}
