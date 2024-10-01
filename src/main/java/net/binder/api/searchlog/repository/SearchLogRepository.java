package net.binder.api.searchlog.repository;

import net.binder.api.searchlog.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
}
