package net.binder.api.auth.service;

import lombok.RequiredArgsConstructor;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.entity.SocialAccount;
import net.binder.api.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialMemberService {

    private final MemberRepository memberRepository;

    public Member findBySocialAccountOrEmail(String provider, String providerId, String email) {
        // 소셜 계정으로 검색 후 멤버가 있다면 반환, 없다면 기존 멤버과 연동 시도
        return memberRepository.findBySocialAccount(provider, providerId)
                .orElseGet(() -> link(provider, providerId, email));
    }

    private Member link(String provider, String providerId, String email) {
        // 이메일고 일치하는 멤버가 있다면 소셜 계정 연결 후 반환

        return memberRepository.findByEmail(email)
                .map(member -> {
                    SocialAccount socialAccount = new SocialAccount(provider, providerId);
                    member.linkSocialAccount(socialAccount);
                    return member;
                })
                .orElse(null);
    }

    public Member register(String provider, String providerId, String email) {

        String uniqueNickname = generateUniqueNickname(provider, providerId);

        Member member = Member.builder()
                .email(email)
                .nickname(uniqueNickname)
                .role(Role.ROLE_USER)
                .build();

        SocialAccount socialAccount = new SocialAccount(provider, providerId);

        member.linkSocialAccount(socialAccount);

        memberRepository.save(member);
        return member;

    }

    private String generateUniqueNickname(String provider, String providerId) {
        return provider + "_" + providerId;
    }
}
