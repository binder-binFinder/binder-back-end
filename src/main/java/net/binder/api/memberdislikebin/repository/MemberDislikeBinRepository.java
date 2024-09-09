package net.binder.api.memberdislikebin.repository;

import net.binder.api.memberdislikebin.entity.MemberDislikeBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberDislikeBinRepository extends JpaRepository<MemberDislikeBin, Long> {

    void deleteMemberLikeBinByMember_EmailAndBin_Id(String memberEmail, Long binId);

    boolean existsByMember_IdAndBin_Id(Long memberId, Long binId);
}
