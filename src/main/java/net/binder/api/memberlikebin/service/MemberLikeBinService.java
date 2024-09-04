package net.binder.api.memberlikebin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.service.BinService;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import net.binder.api.memberlikebin.entity.MemberLikeBin;
import net.binder.api.memberlikebin.repository.MemberLikeBinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberLikeBinService {

    private final MemberLikeBinRepository memberLikeBinRepository;
    private final MemberService memberService;
    private final BinService binService;

    public void upLike(String email, Long binId){
        Member member = memberService.findByEmail(email);
        Bin bin = binService.findById(binId);
        bin.upLike();

        MemberLikeBin memberLikeBin = MemberLikeBin.builder().
                member(member).
                bin(bin).
                build();
        memberLikeBinRepository.save(memberLikeBin);
    }
}
