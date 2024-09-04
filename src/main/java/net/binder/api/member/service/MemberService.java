package net.binder.api.member.service;

import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
import net.binder.api.member.dto.MemberProfile;
import net.binder.api.member.dto.MemberTimeLine;
import net.binder.api.member.entity.Member;
import net.binder.api.member.repository.MemberRepository;
import net.binder.api.memberlikebin.repository.BookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    private final BookmarkRepository bookmarkRepository;

    @Transactional(readOnly = true)
    public MemberProfile getProfile(String email) {
        Member member = findByEmail(email);
        Long count = bookmarkRepository.countByMember(member);
        return MemberProfile.from(member, count);
    }

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("이메일과 일치하는 사용자를 찾을 수 없습니다."));
    }

    public void deleteMember(String email, String input) {
        validateInvalidInput(input);

        Member member = findByEmail(email);
        boolean deleted = member.softDelete();

        validateAlreadyDeleted(deleted);
    }

    public void updateProfile(String email, String nickname, String imageUrl) {

        Member member = findByEmail(email);

        validateNicknamePattern(nickname);
        validateDuplicateNickname(nickname, member);

        member.changeProfile(nickname, imageUrl);
    }

    private void validateNicknamePattern(String nickname) {
        String regex = "^[a-zA-Z0-9가-힣]{2,16}$";
        if (!Pattern.matches(regex, nickname)) {
            throw new BadRequestException("닉네임은 영문, 숫자, 한글만 사용하여 2~16자로 구성되어야 합니다.");
        }

    }

    @Transactional(readOnly = true)
    public List<MemberTimeLine> getTimeLines(String email) {
        Member member = findByEmail(email);
        return memberRepository.findTimeLines(member);
    }

    private void validateInvalidInput(String input) {
        if (!input.equals("탈퇴하기")) {
            throw new BadRequestException("탈퇴하기 문구가 올바르게 입력되지 않았습니다.");
        }
    }

    private void validateAlreadyDeleted(boolean deleted) {
        if (!deleted) {
            throw new BadRequestException("이미 탈퇴한 회원입니다.");
        }
    }

    private void validateDuplicateNickname(String nickname, Member member) {
        if (!member.isOwnNickname(nickname) && memberRepository.existsByNickname(nickname)) {
            throw new BadRequestException("이미 존재하는 닉네임입니다.");
        }
    }
}
