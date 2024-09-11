package net.binder.api.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.repository.AdminBinComplaintQueryRepository;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.complaint.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminBinComplaintsService {

    private static final long MIN_EXPOSED_COMPLAINT_COUNT = 3;

    private final AdminBinComplaintQueryRepository adminBinComplaintRepository;

    private final ComplaintRepository complaintRepository;

    public List<BinComplaintDetail> getBinComplaintDetails(ComplaintFilter filter) {
        return adminBinComplaintRepository.findAll(filter, MIN_EXPOSED_COMPLAINT_COUNT);
    }

    public Long getComplaintPendingCount() {
        return complaintRepository.countByCountAndStatus(MIN_EXPOSED_COMPLAINT_COUNT, ComplaintStatus.PENDING);
    }
}
