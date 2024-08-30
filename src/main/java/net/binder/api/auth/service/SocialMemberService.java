package net.binder.api.auth.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.entity.SocialAccount;
import net.binder.api.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SocialMemberService {

    private final MemberRepository memberRepository;

    public Member getMember(String provider, String providerId, String email, String nickname) {
        // 소셜 계정으로 검색 후 멤버가 있다면 반환, 없다면 기존 멤버과 연동 시도
        return memberRepository.findBySocialAccount(provider, providerId)
                .orElseGet(() -> linkOrRegister(provider, providerId, email, nickname));
    }

    private Member linkOrRegister(String provider, String providerId, String email, String nickname) {
        // 이메일고 일치하는 멤버가 있다면 소셜 계정 연결 후 반환, 없다면 신규 등록

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> register(email, nickname));

        SocialAccount socialAccount = SocialAccount.builder()
                .provider(provider)
                .providerId(providerId)
                .build();

        member.linkSocialAccount(socialAccount);

        return member;
    }

    private Member register(String email, String nickname) {
        Member member = Member.builder()
                .email(email)
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .build();

        memberRepository.save(member);
        return member;
    }
}
