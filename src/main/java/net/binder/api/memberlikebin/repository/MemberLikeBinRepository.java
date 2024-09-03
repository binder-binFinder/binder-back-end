package net.binder.api.memberlikebin.repository;

import net.binder.api.member.entity.Member;
import net.binder.api.memberlikebin.entity.MemberLikeBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLikeBinRepository extends JpaRepository<MemberLikeBin, Long> {

    long countByMember(Member member);
}
