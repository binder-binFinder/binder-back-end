package net.binder.api.bin.repository;

import net.binder.api.bin.dto.BinDetailResponseForLoginUser;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BinRepository extends JpaRepository<Bin, Long> {

    @Query("""
            SELECT b FROM Bin b 
                WHERE b.deletedAt IS NULL 
                AND b.id = :id
                """)
    Optional<Bin> findByIdAndNotDeleted(Long id);

    @Query(value = """
    SELECT b.id, b.created_at, b.modified_at, b.title, b.type,
           ST_X(b.point) as latitude, ST_Y(b.point) as longitude, b.address,
           b.like_count, b.dislike_count, b.image_url,
           CASE WHEN mlb.id IS NOT NULL THEN 1 ELSE 0 END as is_liked,
           CASE WHEN mdb.id IS NOT NULL THEN 1 ELSE 0 END as is_disliked,
           CASE WHEN mb.id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked
    FROM bin b
    LEFT JOIN member_like_bin mlb ON b.id = mlb.bin_id AND mlb.member_id = :memberId
    LEFT JOIN member_dislike_bin mdb ON b.id = mdb.bin_id AND mdb.member_id = :memberId
    LEFT JOIN book_mark mb ON b.id = mb.bin_id AND mb.member_id = :memberId
    WHERE b.deleted_at IS NULL AND b.id = :binId
    """, nativeQuery = true)
    Optional<BinDetailProjection> findDetailByIdAndMemberIdNative(@Param("binId") Long binId, @Param("memberId") Long memberId);
}
