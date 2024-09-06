package net.binder.api.binregistration.repository;

import java.util.List;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BinRegistrationRepository extends JpaRepository<BinRegistration, Long> {

    @Query("""
            SELECT br FROM BinRegistration br
            WHERE br.member = :member AND br.bin.deletedAt IS NULL
            ORDER BY br.bin.createdAt DESC
            """)
    List<BinRegistration> findAllByMember(Member member);
}
