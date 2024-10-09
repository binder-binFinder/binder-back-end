package net.binder.api.likeanddislike.repository;

import net.binder.api.likeanddislike.entity.MemberLikeBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLikeBinRepository extends JpaRepository<MemberLikeBin, Long> {


    void deleteMemberLikeBinByMember_IdAndBin_Id(Long memberId, Long binId);

    boolean existsByMember_IdAndBin_Id(Long memberId, Long binId);
}

