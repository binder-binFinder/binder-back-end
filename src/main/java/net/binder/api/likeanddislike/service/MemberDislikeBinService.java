package net.binder.api.likeanddislike.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.likeanddislike.repository.MemberDislikeBinRepository;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.likeanddislike.entity.MemberDislikeBin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberDislikeBinService {

    private final MemberDislikeBinRepository memberDislikeBinRepository;

    private final BinService binService;

    public void createDislike(Member member, Long binId){

        Bin bin = binService.findById(binId);
        bin.increaseDislike();
        MemberDislikeBin memberDislikeBin = MemberDislikeBin.builder()
                .bin(bin)
                .member(member)
                .build();
        memberDislikeBinRepository.save(memberDislikeBin);
    }

    public void deleteDisLike(Long memberId, Long binId){
        Bin bin = binService.findById(binId);
        bin.decreaseDisLike();
        memberDislikeBinRepository.deleteMemberLikeBinByMember_IdAndBin_Id(memberId, binId);
    }

    public boolean isDislikeExist(Long memberId, Long binId){
        return memberDislikeBinRepository.existsByMember_IdAndBin_Id(memberId, binId);
    }
}
