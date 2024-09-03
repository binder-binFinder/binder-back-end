package net.binder.api.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.entity.Member;
import net.binder.api.member.entity.Role;
import net.binder.api.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@example.com", "테스트", Role.ROLE_USER, "http://example.com/image.jpg");
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("프로필 업데이트 성공 - 닉네임과 이미지 URL 모두 변경")
    void updateProfile_success() {
        // given
        String newNickname = "새로운닉네임";
        String newImageUrl = "http://example.com/new-image.jpg";

        // when
        memberService.updateProfile(testMember.getEmail(), newNickname, newImageUrl);

        // then
        Member updatedMember = memberRepository.findByEmail(testMember.getEmail()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo(newNickname);
        assertThat(updatedMember.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("프로필 업데이트 성공 - 닉네임만 변경")
    void updateProfile_nickname() {
        // given
        String newNickname = "새로운닉네임";

        // when
        memberService.updateProfile(testMember.getEmail(), newNickname, testMember.getImageUrl());

        // then
        Member updatedMember = memberRepository.findByEmail(testMember.getEmail()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo(newNickname);
        assertThat(updatedMember.getImageUrl()).isEqualTo(testMember.getImageUrl());
    }

    @Test
    @DisplayName("프로필 업데이트 성공 - 이미지 URL만 변경")
    void updateProfile_imageUrl() {
        // given
        String newImageUrl = "http://example.com/new-image.jpg";

        // when
        memberService.updateProfile(testMember.getEmail(), testMember.getNickname(), newImageUrl);

        // then
        Member updatedMember = memberRepository.findByEmail(testMember.getEmail()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo(testMember.getNickname());
        assertThat(updatedMember.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("프로필 업데이트 실패 - 중복된 닉네임")
    void updateProfile_duplicateNickname() {
        // given
        String duplicateNickname = "중복닉네임";
        memberRepository.save(
                new Member("another@example.com", duplicateNickname, Role.ROLE_USER, "http://example.com/another.jpg"));

        // when & then
        assertThatThrownBy(
                () -> memberService.updateProfile(testMember.getEmail(), duplicateNickname, testMember.getImageUrl()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("프로필 업데이트 실패 - 존재하지 않는 회원")
    void updateProfile_notFound() {
        // given
        String nonExistentEmail = "nonexistent@example.com";

        // when & then
        assertThatThrownBy(() -> memberService.updateProfile(nonExistentEmail, "새닉네임", "http://example.com/new.jpg"))
                .isInstanceOf(NotFoundException.class);
    }
}