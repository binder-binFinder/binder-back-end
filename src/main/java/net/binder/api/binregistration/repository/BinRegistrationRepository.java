package net.binder.api.binregistration.repository;

import java.util.List;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import net.binder.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BinRegistrationRepository extends JpaRepository<BinRegistration, Long> {

    @Query("""
            SELECT br FROM BinRegistration br
            JOIN FETCH br.bin
            WHERE br.member = :member
            ORDER BY br.bin.createdAt DESC
            """)
    List<BinRegistration> findAllByMember(Member member);

    Long countByStatus(BinRegistrationStatus status);
}
