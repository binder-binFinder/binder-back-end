package net.binder.api.bookmark.repository;

import net.binder.api.bookmark.entity.BookMark;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<BookMark, Long> {

    Long countByMember(Member member);
}
