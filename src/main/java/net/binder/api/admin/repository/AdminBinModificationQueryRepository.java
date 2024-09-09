package net.binder.api.admin.repository;

import static net.binder.api.binmodification.entity.QBinModification.binModification;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.binmodification.entity.BinModification;
import net.binder.api.binmodification.entity.BinModificationStatus;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminBinModificationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<BinModification> findAll(ModificationFilter filter) {

        BooleanBuilder booleanBuilder = getBooleanBuilder(filter);

        return jpaQueryFactory.selectFrom(binModification)
                .join(binModification.member).fetchJoin()
                .where(booleanBuilder)
                .orderBy(binModification.createdAt.desc())
                .fetch();
    }

    private BooleanBuilder getBooleanBuilder(ModificationFilter filter) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (filter == ModificationFilter.ENTIRE) {
            return booleanBuilder;
        }
        if (filter == ModificationFilter.PENDING) {
            return booleanBuilder.and(binModification.status.eq(BinModificationStatus.PENDING));
        }
        return booleanBuilder.and(
                binModification.status.in(BinModificationStatus.APPROVED, BinModificationStatus.REJECTED));
    }
}
