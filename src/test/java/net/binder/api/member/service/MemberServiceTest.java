package net.binder.api.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.bin.repository.BinRepository;
import net.binder.api.bin.util.PointUtil;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.binregistration.repository.BinRegistrationRepository;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.dto.BinRegistrationActivity;
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

    @Autowired
    private BinRepository binRepository;

    @Autowired
    private BinRegistrationRepository binRegistrationRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@example.com", "테스트", Role.ROLE_USER, "http://example.com/image.jpg");
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("프로필 업데이트 실패 - 닉네임 패턴 불일치 (짧은 길이)")
    void updateProfile_invalidNicknameTooShort() {
        // given
        String invalidNickname = "a";

        // when & then
        assertThatThrownBy(() ->
                memberService.updateProfile(testMember.getEmail(), invalidNickname, testMember.getImageUrl()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("프로필 업데이트 실패 - 닉네임 패턴 불일치 (긴 길이)")
    void updateProfile_invalidNicknameTooLong() {
        // given
        String invalidNickname = "abcdefghijklmnopq"; // 17자

        // when & then
        assertThatThrownBy(() ->
                memberService.updateProfile(testMember.getEmail(), invalidNickname, testMember.getImageUrl()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("프로필 업데이트 실패 - 닉네임 패턴 불일치 (특수문자 포함)")
    void updateProfile_invalidNicknameSpecialCharacters() {
        // given
        String invalidNickname = "test@user";

        // when & then
        assertThatThrownBy(() ->
                memberService.updateProfile(testMember.getEmail(), invalidNickname, testMember.getImageUrl()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("프로필 업데이트 성공 - 유효한 닉네임 (한글)")
    void updateProfile_validNicknameKorean() {
        // given
        String validNickname = "테스트사용자";

        // when
        memberService.updateProfile(testMember.getEmail(), validNickname, testMember.getImageUrl());

        // then
        Member updatedMember = memberRepository.findByEmail(testMember.getEmail()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo(validNickname);
    }

    @Test
    @DisplayName("프로필 업데이트 성공 - 유효한 닉네임 (영문, 숫자, 한글 혼합)")
    void updateProfile_validNicknameMixed() {
        // given
        String validNickname = "test사용자123";

        // when
        memberService.updateProfile(testMember.getEmail(), validNickname, testMember.getImageUrl());

        // then
        Member updatedMember = memberRepository.findByEmail(testMember.getEmail()).orElseThrow();
        assertThat(updatedMember.getNickname()).isEqualTo(validNickname);
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

    @Test
    @DisplayName("사용자가 작성한 쓰레기통 목록을 최신 날짜순으로 가져온다.")
    void findTimeLines() {
        // Given

        Bin bin1 = getBin("Bin1", BinType.GENERAL, "Address1", 5L, "image1", 127.1, 37.4);
        Bin bin2 = getBin("Bin2", BinType.RECYCLE, "Address2", 10L, "image2", 127.2, 37.5);
        Bin deletedBin = getBin("Bin3", BinType.RECYCLE, "Address3", 15L, "image3", 127.3, 37.6);
        deletedBin.softDelete();

        binRepository.saveAll(List.of(bin1, bin2, deletedBin));

        BinRegistration br1 = new BinRegistration(testMember, bin1, BinRegistrationStatus.APPROVED);
        BinRegistration br2 = new BinRegistration(testMember, bin2, BinRegistrationStatus.PENDING);
        BinRegistration brDeleted = new BinRegistration(testMember, deletedBin, BinRegistrationStatus.APPROVED);
        binRegistrationRepository.saveAll(List.of(br1, br2, brDeleted));

        // When
        List<BinRegistrationActivity> result = memberService.getRegistrationActivities("test@example.com");

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("title").containsExactly("Bin3", "Bin2", "Bin1");
        assertThat(result).extracting("bookmarkCount").containsExactly(15L, 10L, 5L);
        assertThat(result).extracting(BinRegistrationActivity::getIsDeleted).containsExactly(true, false, false);
    }

    private Bin getBin(String title, BinType type, String address, Long bookmarkCount, String imageUrl,
                       Double longitude, Double latitude) {
        return Bin.builder()
                .title(title)
                .point(PointUtil.getPoint(longitude, latitude))
                .type(type)
                .address(address)
                .bookmarkCount(bookmarkCount)
                .imageUrl(imageUrl)
                .build();
    }
}