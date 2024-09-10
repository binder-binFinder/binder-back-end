package net.binder.api.complaint.repository;

import java.util.List;
import net.binder.api.complaint.entity.ComplaintInfo;
import net.binder.api.complaint.entity.ComplaintType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintInfoRepository extends JpaRepository<ComplaintInfo, Long> {

    boolean existsByMemberIdAndType(Long memberId, ComplaintType type);

    List<ComplaintInfo> findAllByComplaintId(Long complaintId);
}
