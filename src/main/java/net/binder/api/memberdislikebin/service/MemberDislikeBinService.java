package net.binder.api.memberdislikebin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.memberdislikebin.entity.MemberDislikeBin;
import net.binder.api.memberdislikebin.repository.MemberDislikeBinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberDislikeBinService {

    private final MemberDislikeBinRepository memberDislikeBinRepository;

    private final MemberService memberService;

    private final BinService binService;

    public void saveDislike(String email, Long binId){
        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);
        bin.increaseDislike();
        MemberDislikeBin memberDislikeBin = MemberDislikeBin.builder()
                .bin(bin)
                .member(member)
                .build();
        memberDislikeBinRepository.save(memberDislikeBin);
    }

    public void deleteDisLike(String email, Long binId){
        Bin bin = binService.findById(binId);
        bin.decreaseDisLike();
        memberDislikeBinRepository.deleteMemberLikeBinByMember_EmailAndBin_Id(email, binId);
    }
}
