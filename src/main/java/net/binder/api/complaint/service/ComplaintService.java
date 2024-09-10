package net.binder.api.complaint.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintType;
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

    private final MemberService memberService;

    private final BinService binService;

    public Complaint createComplaint(String email, Long binId, ComplaintType type) {

        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);

        validateDuplicateComplaint(member.getId(), binId, type);

        Complaint complaint = new Complaint(member, bin, type);

        complaintRepository.save(complaint);

        return complaint;
    }

    private void validateDuplicateComplaint(Long memberId, Long binId, ComplaintType type) {
        if (complaintRepository.existsByMemberIdAndBinIdAndType(memberId, binId, type)) {
            throw new BadRequestException("동일한 신고 내역이 존재합니다.");
        }
    }
}
