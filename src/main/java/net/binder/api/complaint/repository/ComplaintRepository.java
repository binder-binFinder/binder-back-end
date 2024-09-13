package net.binder.api.complaint.repository;

import java.util.Optional;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByBinIdAndStatus(Long binId, ComplaintStatus complaintStatus);

    Long countByCountGreaterThanEqualAndStatus(long count, ComplaintStatus status);
}
