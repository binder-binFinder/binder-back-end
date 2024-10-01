package net.binder.api.bookmark.repository;

import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("""
            SELECT COUNT(*) FROM Bookmark b
            left join b.bin bi
            left join b.member m
            where m = :member and bi.deletedAt is null
            """)
    Long countByMember(Member member);

    void deleteByMember_EmailAndBin_Id(String email, Long binId);

    boolean existsByMember_EmailAndBin_Id(String email, Long binId);
}
