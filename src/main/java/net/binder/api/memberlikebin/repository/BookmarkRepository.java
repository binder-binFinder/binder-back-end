package net.binder.api.memberlikebin.repository;

import net.binder.api.member.entity.Member;
import net.binder.api.memberlikebin.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    long countByMember(Member member);
}
