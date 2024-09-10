package net.binder.api.admin.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AdminBinComplaintQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

//    public List<BinComplaintDetail> findAll(ComplaintFilter filter, Long minComplaintCount) {
//
//    }
}
