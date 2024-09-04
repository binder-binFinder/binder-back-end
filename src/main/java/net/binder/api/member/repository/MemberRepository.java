package net.binder.api.member.repository;

import java.util.List;
import java.util.Optional;
import net.binder.api.member.dto.MemberTimeLine;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            SELECT m FROM Member m
            JOIN m.socialAccounts sa
            WHERE sa.provider = :provider
            AND sa.providerId = :providerId
            """)
    Optional<Member> findBySocialAccount(String provider, String providerId);

    Optional<Member> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query("""
            SELECT new net.binder.api.member.dto.MemberTimeLine(
                mcb.bin.id, mcb.bin.title, mcb.bin.address, mcb.bin.type,
                mcb.status, mcb.createdAt,
                mcb.bin.bookmarkCount
            )
            FROM Member m
            JOIN MemberCreateBin mcb ON m = mcb.member
            WHERE m = :member
            AND mcb.bin.deletedAt IS NULL
            ORDER BY mcb.createdAt DESC
            """)
    List<MemberTimeLine> findTimeLines(Member member);
}
