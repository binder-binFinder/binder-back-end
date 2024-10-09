package net.binder.api.likeanddislike.repository;

import net.binder.api.likeanddislike.entity.MemberDislikeBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDislikeBinRepository extends JpaRepository<MemberDislikeBin, Long> {

    void deleteMemberLikeBinByMember_IdAndBin_Id(Long memberId, Long binId);

    boolean existsByMember_IdAndBin_Id(Long memberId, Long binId);
}
