package net.binder.api.complaint.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.complaint.repository.ComplaintRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ComplaintCountReader {

    private final ComplaintRepository complaintRepository;

    @Transactional(readOnly = true)
    public Long getComplaintCount(Bin bin) {
        Optional<Complaint> optionalComplaint = complaintRepository.findByBinIdAndStatus(bin.getId(),
                ComplaintStatus.PENDING); // 현재 진행중인 신고가 있는지 확인

        if (optionalComplaint.isEmpty()) { // 없으면 0 반환
            return 0L;
        }
        return optionalComplaint.get().getCount();
    }
}
