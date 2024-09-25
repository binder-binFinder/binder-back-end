package net.binder.api.bin.repository;

import java.util.Optional;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BinRepository extends JpaRepository<Bin, Long> {

    Optional<Bin> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
                SELECT b.id as id, b.createdAt as createdAt, b.modifiedAt as modifiedAt, 
                       b.title as title, b.type as type,
                       function('ST_X', b.point) as latitude, function('ST_Y', b.point) as longitude, b.address as address,
                       b.likeCount as likeCount, b.dislikeCount as dislikeCount, 
                       b.bookmarkCount as bookmarkCount, 
                       CASE WHEN c.count IS NOT NULL THEN c.count ELSE 0 END as complaintCount, 
                       b.imageUrl as imageUrl,
                       CASE WHEN br.id IS NOT NULL AND br.member.id = :memberId THEN true ELSE false END as isOwner,
                       CASE WHEN mlb.id IS NOT NULL THEN true ELSE false END as isLiked,
                       CASE WHEN mdb.id IS NOT NULL THEN true ELSE false END as isDisliked,
                       CASE WHEN bm.id IS NOT NULL THEN true ELSE false END as isBookmarked
                FROM Bin b
                LEFT JOIN BinRegistration br ON b.id = br.bin.id AND br.member.id = :memberId
                LEFT JOIN MemberLikeBin mlb ON b.id = mlb.bin.id AND mlb.member.id = :memberId
                LEFT JOIN MemberDislikeBin mdb ON b.id = mdb.bin.id AND mdb.member.id = :memberId
                LEFT JOIN Bookmark bm ON b.id = bm.bin.id AND bm.member.id = :memberId
                LEFT JOIN Complaint c ON b = c.bin AND c.status = 'PENDING'
                WHERE b.deletedAt IS NULL AND b.id = :binId
            """)
    Optional<BinDetailProjection> findDetailByIdAndMemberIdNative(@Param("binId") Long binId,
                                                                  @Param("memberId") Long memberId);

}
