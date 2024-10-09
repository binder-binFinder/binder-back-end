package net.binder.api.likeanddislike.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.service.MemberService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Component
public class LikeAndDislikeFacade {

    private final MemberService memberService;
    private final MemberDislikeBinService memberDislikeBinService;
    private final MemberLikeBinService memberLikeBinService;

    public void createLike(String email, Long binId){

        Member member = memberService.findByEmail(email);
        if (memberLikeBinService.isLikeExist(member.getId(), binId)) {
            throw new BadRequestException("이미 좋아요를 누른 쓰레기통입니다.");
        }
        if(memberDislikeBinService.isDislikeExist(member.getId(), binId)){
            memberDislikeBinService.deleteDisLike(member.getId(), binId);
        }
        memberLikeBinService.createLike(member, binId);
    }

    public void deleteLike(String email, Long binId){
        Member member = memberService.findByEmail(email);
        if (!memberLikeBinService.isLikeExist(member.getId(), binId)) {
            throw new BadRequestException("좋아요를 누르지 않았던 쓰레기통입니다.");
        }
        memberLikeBinService.deleteLike(member.getId(), binId);
    }

    public void createDislike(String email, Long binId){

        Member member = memberService.findByEmail(email);

        if (memberDislikeBinService.isDislikeExist(member.getId(), binId)) {
            throw new BadRequestException("이미 좋아요를 누른 쓰레기통입니다.");
        }
        if(memberLikeBinService.isLikeExist(member.getId(), binId)){
            memberLikeBinService.deleteLike(member.getId(), binId);
        }

        memberDislikeBinService.createDislike(member, binId);
    }

    public void deleteDislike(String email, Long binId){
        Member member = memberService.findByEmail(email);
        if (!memberDislikeBinService.isDislikeExist(member.getId(), binId)) {
            throw new BadRequestException("싫어요를 누르지 않았던 쓰레기통입니다.");
        }
        memberDislikeBinService.deleteDisLike(member.getId(), binId);
    }

}
