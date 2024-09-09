package net.binder.api.bookmark.repository;

import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Long countByMember(Member member);
}
