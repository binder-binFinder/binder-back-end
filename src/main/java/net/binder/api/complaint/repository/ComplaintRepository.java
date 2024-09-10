package net.binder.api.complaint.repository;

import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    boolean existsByMemberIdAndBinIdAndType(Long memberId, Long binId, ComplaintType type);
}
