package net.binder.api.bookmark.repository;

import net.binder.api.bin.entity.BinDetailProjection;
import net.binder.api.bookmark.dto.BookmarkProjection;
import net.binder.api.bookmark.dto.BookmarkResponse;
import net.binder.api.bookmark.entity.Bookmark;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Long countByMember(Member member);

    void deleteByMember_EmailAndBin_Id(String email, Long binId);

    boolean existsByMember_EmailAndBin_Id(String email, Long binId);

    @Query("""
             select bi.id as binId, bi.address as address, bi.title as title, bi.type as binType,
        st_distance(bi.point, st_geomfromtext(CONCAT('POINT(', :latitude, ' ', :longitude, ')'), 4326)) as distance
        from Bookmark b
        left join Bin bi on b.bin.id = bi.id
        left join Member m on b.member.id = m.id
        where m.email = :email
        """)
    Optional<List<BookmarkProjection>> findBookmarkByMember_Email(@Param("email") String email,
                                                    @Param("longitude") Double longitude,
                                                    @Param("latitude") Double latitude);


}
