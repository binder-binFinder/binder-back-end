package net.binder.api.bin.repository;

import java.util.Optional;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BinRepository extends JpaRepository<Bin, Long> {

    Optional<Bin> findByIdAndDeletedAtIsNull(Long id);

    @Query(value = """
            SELECT b.id, b.created_at, b.modified_at, b.title, b.type,
                   ST_X(b.point) as longitude, ST_Y(b.point) as latitude, b.address,
                   b.like_count, b.dislike_count, b.bookmark_count, b.image_url,
                   CASE WHEN br.id IS NOT NULL AND br.member_id = :memberId THEN 1 ELSE 0 END as is_owner,
                   CASE WHEN mlb.id IS NOT NULL THEN 1 ELSE 0 END as is_liked,
                   CASE WHEN mdb.id IS NOT NULL THEN 1 ELSE 0 END as is_disliked,
                   CASE WHEN bm.id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked
            FROM bin b
            LEFT JOIN bin_registration br ON b.id = br.bin_id AND br.member_id = :memberId
            LEFT JOIN member_like_bin mlb ON b.id = mlb.bin_id AND mlb.member_id = :memberId
            LEFT JOIN member_dislike_bin mdb ON b.id = mdb.bin_id AND mdb.member_id = :memberId
            LEFT JOIN bookmark bm ON b.id = bm.bin_id AND bm.member_id = :memberId
            WHERE b.deleted_at IS NULL AND b.id = :binId
            """, nativeQuery = true)
    Optional<BinDetailProjection> findDetailByIdAndMemberIdNative(@Param("binId") Long binId,
                                                                  @Param("memberId") Long memberId);
}
