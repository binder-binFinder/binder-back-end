package net.binder.api.bookmark.repository;

import net.binder.api.bin.entity.BinDetailProjection;
import net.binder.api.bookmark.dto.BookmarkProjection;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Long countByMember(Member member);

    void deleteByMember_EmailAndBin_Id(String email, Long binId);

    boolean existsByMember_EmailAndBin_Id(String email, Long binId);
}
