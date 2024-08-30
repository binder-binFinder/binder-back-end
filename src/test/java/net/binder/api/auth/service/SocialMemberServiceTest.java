package net.binder.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.entity.SocialAccount;
import net.binder.api.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SocialMemberServiceTest {

    @Autowired
    private SocialMemberService socialMemberService;

    @Autowired
    private MemberRepository memberRepository;

    private final String PROVIDER = "google";
    private final String PROVIDER_ID = "12345";
    private final String EMAIL = "test@example.com";
    private final String NICKNAME = "test";

    @Test
    @DisplayName("소셜 계정으로 이미 가입한 회원을 찾을 수 있다")
    void getMember_ExistingSocialAccount() {
        // Given
        Member existingMember = createMemberWithSocialAccount();

        // When
        Member result = socialMemberService.getMember(PROVIDER, PROVIDER_ID, EMAIL, NICKNAME);

        // Then
        assertThat(result).isEqualTo(existingMember);
    }

    @Test
    @DisplayName("이메일로 가입한 회원에게 소셜 계정을 연결할 수 있다")
    void getMember_LinkSocialAccountToExistingMember() {
        // Given
        Member existingMember = createMember();

        // When
        Member result = socialMemberService.getMember(PROVIDER, PROVIDER_ID, EMAIL, NICKNAME);

        // Then
        assertThat(result).isEqualTo(existingMember);
    }

    @Test
    @DisplayName("새로운 소셜 회원을 등록할 수 있다")
    void getMember_RegisterNewMember() {

        // When
        Member result = socialMemberService.getMember(PROVIDER, PROVIDER_ID, EMAIL, NICKNAME);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getNickname()).isEqualTo(NICKNAME);
        assertThat(result.getRole()).isEqualTo(Role.ROLE_USER);
        assertThat(result.getSocialAccounts().get(0).getProvider()).isEqualTo(PROVIDER);
        assertThat(result.getSocialAccounts().get(0).getProviderId()).isEqualTo(PROVIDER_ID);
    }

    @Test
    @DisplayName("이미 소셜 계정이 연동된 회원에게 새로운 소셜 계정을 추가할 수 있다")
    void addSocialAccount_ToExistingMemberWithSocialAccount() {
        // Given
        createMemberWithSocialAccount();
        String newProvider = "facebook";
        String newProviderId = "67890";

        // When
        Member updatedMember = socialMemberService.getMember(newProvider, newProviderId, EMAIL, "test2");

        // Then
        assertThat(updatedMember.getSocialAccounts()).hasSize(2);
        assertThat(updatedMember.getSocialAccounts())
                .extracting("provider", "providerId")
                .containsExactlyInAnyOrder(
                        tuple(PROVIDER, PROVIDER_ID),
                        tuple(newProvider, newProviderId));
    }

    private Member createMember() {
        Member member = Member.builder()
                .email(EMAIL)
                .nickname(NICKNAME)
                .role(Role.ROLE_USER)
                .build();

        return memberRepository.save(member);
    }

    private Member createMemberWithSocialAccount() {
        Member member = Member.builder()
                .email(EMAIL)
                .nickname(NICKNAME)
                .role(Role.ROLE_USER)
                .build();

        SocialAccount socialAccount = SocialAccount.builder()
                .provider(PROVIDER)
                .providerId(PROVIDER_ID)
                .build();

        member.linkSocialAccount(socialAccount);
        return memberRepository.save(member);
    }

}