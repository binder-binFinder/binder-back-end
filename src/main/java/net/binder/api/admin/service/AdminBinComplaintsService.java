package net.binder.api.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.dto.TypeCount;
import net.binder.api.admin.repository.AdminBinComplaintQueryRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.complaint.repository.ComplaintRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.notification.entity.NotificationType;
import net.binder.api.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminBinComplaintsService {

    private static final long MIN_EXPOSED_COMPLAINT_COUNT = 3;

    private final AdminBinComplaintQueryRepository adminBinComplaintRepository;

    private final ComplaintRepository complaintRepository;

    private final MemberService memberService;

    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<BinComplaintDetail> getBinComplaintDetails(ComplaintFilter filter) {
        return adminBinComplaintRepository.findAll(filter, MIN_EXPOSED_COMPLAINT_COUNT);
    }

    @Transactional(readOnly = true)
    public Long getComplaintPendingCount() {
        return complaintRepository.countByCountAndStatus(MIN_EXPOSED_COMPLAINT_COUNT, ComplaintStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<TypeCount> getBinComplaintCountsPerType(Long id) {
        Complaint complaint = getComplaint(id);

        return adminBinComplaintRepository.getTypeCounts(complaint);
    }

    public void approve(String email, Long id, String approveReason) {
        Member admin = memberService.findByEmail(email);
        Complaint complaint = getComplaint(id);

        validateComplaintStatus(complaint);
        complaint.approve(); // 승인시 Bin을 softDelete

        List<Member> receivers = adminBinComplaintRepository.findMembers(complaint);

        notificationService.sendNotificationForUsers(admin, receivers, complaint.getBin(),
                NotificationType.BIN_COMPLAINT_APPROVED, approveReason);

    }

    public void reject(String email, Long id, String rejectReason) {
        Member admin = memberService.findByEmail(email);
        Complaint complaint = getComplaint(id);

        validateComplaintStatus(complaint);
        complaint.reject();

        List<Member> receivers = adminBinComplaintRepository.findMembers(complaint);

        notificationService.sendNotificationForUsers(admin, receivers, complaint.getBin(),
                NotificationType.BIN_COMPLAINT_REJECTED, rejectReason);
    }

    private Complaint getComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("일치하는 신고 내역이 없습니다."));
    }

    private void validateComplaintStatus(Complaint complaint) {
        if (!complaint.isPending()) {
            throw new BadRequestException("이미 심사가 완료된 상태입니다.");
        }
    }
}
