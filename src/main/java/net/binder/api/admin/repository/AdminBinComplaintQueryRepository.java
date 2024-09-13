package net.binder.api.admin.repository;

import static net.binder.api.complaint.entity.QComplaint.complaint;
import static net.binder.api.complaint.entity.QComplaintInfo.complaintInfo;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.BinComplaintDetail;
import net.binder.api.admin.dto.ComplaintFilter;
import net.binder.api.admin.dto.TypeCount;
import net.binder.api.complaint.entity.Complaint;
import net.binder.api.complaint.entity.ComplaintStatus;
import net.binder.api.member.entity.Member;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AdminBinComplaintQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<BinComplaintDetail> findAll(ComplaintFilter filter, Long minComplaintCount) {
        BooleanBuilder booleanBuilder = getBooleanBuilder(filter, minComplaintCount);

        return jpaQueryFactory
                .select(Projections.constructor(BinComplaintDetail.class,
                        complaint.id,
                        complaint.bin.id,
                        complaint.bin.title,
                        complaint.bin.address,
                        complaint.bin.type,
                        complaint.status,
                        complaint.bin.imageUrl,
                        complaintInfo.createdAt,
                        complaint.count))
                .from(complaint)
                .join(complaint.bin)
                .join(complaintInfo).on(complaintInfo.complaint.eq(complaint)
                        .and(complaintInfo.createdAt.eq(selectMaxCreatedAt())))
                .where(booleanBuilder)
                .orderBy(complaintInfo.id.desc())
                .fetch();

    }

    public List<TypeCount> getTypeCounts(Complaint complaint) {
        return jpaQueryFactory
                .select(Projections.constructor(TypeCount.class,
                        complaintInfo.type,
                        complaintInfo.type.count()))
                .from(complaintInfo)
                .where(complaintInfo.complaint.eq(complaint))
                .groupBy(complaintInfo.type)
                .fetch();
    }

    public List<Member> findMembers(Complaint complaint) {
        return jpaQueryFactory.select(complaintInfo.member)
                .from(complaintInfo)
                .where(complaintInfo.complaint.eq(complaint))
                .fetch();
    }

    private JPQLQuery<LocalDateTime> selectMaxCreatedAt() {
        return JPAExpressions
                .select(complaintInfo.createdAt.max())
                .from(complaintInfo)
                .where(complaintInfo.complaint.eq(complaint));
    }


    private BooleanBuilder getBooleanBuilder(ComplaintFilter filter, Long minComplaintCount) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.and(complaint.count.goe(minComplaintCount));

        if (filter == ComplaintFilter.ENTIRE) {
            return booleanBuilder;
        }
        if (filter == ComplaintFilter.PENDING) {
            return booleanBuilder.and(complaint.status.eq(ComplaintStatus.PENDING));
        }
        return booleanBuilder.and(
                complaint.status.in(ComplaintStatus.APPROVED, ComplaintStatus.REJECTED));
    }
}
