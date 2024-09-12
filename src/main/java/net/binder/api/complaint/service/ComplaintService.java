package net.binder.api.complaint.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintInfo;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.complaint.entity.ComplaintType;
import net.binder.api.complaint.repository.ComplaintInfoRepository;
import net.binder.api.complaint.repository.ComplaintRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    private final ComplaintInfoRepository complaintInfoRepository;

    private final MemberService memberService;

    private final BinService binService;

    public Complaint createComplaint(String email, Long binId, ComplaintType type) {

        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);

        Complaint complaint = getComplaint(binId, bin);

        validateDuplicateComplaint(complaint.getId(), member, type);

        ComplaintInfo complaintInfo = new ComplaintInfo(complaint, member, type);
        complaintInfoRepository.save(complaintInfo);
        complaint.increaseCount();

        return complaint;
    }

    private Complaint getComplaint(Long binId, Bin bin) {
        return complaintRepository.findByBinIdAndStatus(binId, ComplaintStatus.PENDING)
                .orElseGet(() -> {
                    Complaint newComplaint = new Complaint(bin, ComplaintStatus.PENDING, 0L);
                    complaintRepository.save(newComplaint);
                    return newComplaint;
                });
    }

    private void validateDuplicateComplaint(Long complaintId, Member member, ComplaintType type) {
        if (complaintInfoRepository.existsByComplaintIdAndMemberIdAndType(complaintId, member.getId(), type)) {
            throw new BadRequestException("동일한 신고 내역이 존재합니다.");
        }
    }
}
