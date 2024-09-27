package net.binder.api.bookmark.repository;

import jakarta.persistence.QueryHint;
import net.binder.api.bookmark.dto.BookmarkProjection;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.member.entity.Member;
import org.hibernate.jpa.AvailableHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    @Query("""
    SELECT b.id AS bookmarkId, bi.id AS binId, bi.address AS address, bi.title AS title, bi.type AS binType,
    ST_Distance(bi.point, ST_GeomFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326)) AS distance
    FROM Bookmark b
    LEFT JOIN Bin bi ON b.bin.id = bi.id
    LEFT JOIN Member m ON b.member.id = m.id
    WHERE m.email = :email and bi.deletedAt IS null
    AND ST_Contains(ST_Buffer(ST_GeomFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326), :radius), bi.point)
    ORDER BY ST_Distance(bi.point, ST_GeomFromText(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326))
    LIMIT 10
""")
    Optional<List<BookmarkProjection>> findNearByBookmarksByMember_Email(@Param("email") String email,
                                                                         @Param("longitude") Double longitude,
                                                                         @Param("latitude") Double latitude,
                                                                         @Param("radius") int radius);
}
