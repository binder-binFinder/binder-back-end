package net.binder.api.memberlikebin.repository;

import net.binder.api.memberlikebin.entity.MemberLikeBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberLikeBinRepository extends JpaRepository<MemberLikeBin, Long> {

    void deleteMemberLikeBinByMember_EmailAndBin_Id(String memberEmail, Long binId);
}

