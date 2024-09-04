package net.binder.api.membercreatebin.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.Bin;
import net.binder.api.member.entity.Member;
import net.binder.api.membercreatebin.entity.MemberCreateBin;
import net.binder.api.membercreatebin.entity.MemberCreateBinStatus;
import net.binder.api.membercreatebin.repository.MemberCreateBinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCreateBinService {

    private final MemberCreateBinRepository memberCreateBinRepository;

    public void save(Bin bin, Member member){
        MemberCreateBin memberCreateBin = MemberCreateBin.builder()
                .bin(bin)
                .member(member)
                .status(MemberCreateBinStatus.PENDING)
                .build();

        memberCreateBinRepository.save(memberCreateBin);
    }
}
