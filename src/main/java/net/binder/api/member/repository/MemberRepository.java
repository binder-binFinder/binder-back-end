package net.binder.api.member.repository;

import java.util.Optional;
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
}
