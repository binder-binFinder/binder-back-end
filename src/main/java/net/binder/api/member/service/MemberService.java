package net.binder.api.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.exception.BadRequestException;
import net.binder.api.common.exception.NotFoundException;
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
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("이메일과 일치하는 사용자를 찾을 수 없습니다."));
    }

    public void deleteMember(String email) {
        Member member = findByEmail(email);
        boolean deleted = member.softDelete();

        if (!deleted) {
            throw new BadRequestException("이미 탈퇴한 회원입니다.");
        }
    }

    @Transactional(readOnly = true)
    public long calculateLikeCount(Member member) {
        return bookmarkRepository.countByMember(member);
    }

    public void updateProfile(String email, String nickname, String imageUrl) {
        Member member = findByEmail(email);

        if (!member.isOwnNickname(nickname) && memberRepository.existsByNickname(nickname)) {
            throw new BadRequestException("이미 존재하는 닉네임입니다.");
        }

        member.changeProfile(nickname, imageUrl);
    }

    @Transactional(readOnly = true)
    public List<MemberTimeLine> getTimeLines(String email) {
        Member member = findByEmail(email);
        return memberRepository.findTimeLines(member);
    }
}
