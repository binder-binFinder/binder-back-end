package net.binder.api.bin.repository;

import static net.binder.api.admin.dto.RegistrationFilter.ENTIRE;
import static net.binder.api.bin.entity.QBinModification.binModification;
import static net.binder.api.bin.entity.QBinRegistration.binRegistration;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.ModificationFilter;
import net.binder.api.admin.dto.RegistrationFilter;
import net.binder.api.bin.entity.BinModification;
import net.binder.api.bin.entity.BinModificationStatus;
import net.binder.api.bin.entity.BinRegistration;
import net.binder.api.bin.entity.BinRegistrationStatus;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BinQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<BinRegistration> findAll(Long memberId, Long lastBinId, int pageSize) {

        return jpaQueryFactory
                .select(binRegistration)
                .from(binRegistration)
                .join(binRegistration.bin).fetchJoin()
                .where(binRegistration.member.id.eq(memberId), binIdLt(lastBinId))
                .orderBy(binRegistration.bin.id.desc())
                .limit(pageSize)
                .fetch();
    }

    public List<BinRegistration> findAll(RegistrationFilter filter) {

        BooleanBuilder booleanBuilder = getBooleanBuilder(filter);

        return jpaQueryFactory.selectFrom(binRegistration)
                .join(binRegistration.bin).fetchJoin()
                .join(binRegistration.member).fetchJoin()
                .where(booleanBuilder)
                .orderBy(binRegistration.id.desc())
                .fetch();
    }

    public List<BinModification> findAll(ModificationFilter filter) {

        BooleanBuilder booleanBuilder = getBooleanBuilder(filter);

        return jpaQueryFactory.selectFrom(binModification)
                .join(binModification.member).fetchJoin()
                .where(booleanBuilder)
                .orderBy(binModification.id.desc())
                .fetch();
    }

    private BooleanBuilder getBooleanBuilder(RegistrationFilter filter) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (filter == ENTIRE) {
            return booleanBuilder;
        }
        if (filter == RegistrationFilter.PENDING) {
            return booleanBuilder.and(binRegistration.status.eq(BinRegistrationStatus.PENDING));
        }
        return booleanBuilder.and(
                binRegistration.status.in(BinRegistrationStatus.APPROVED, BinRegistrationStatus.REJECTED));
    }

    private BooleanExpression binIdLt(Long lastBinId) {
        if (lastBinId == null) {
            return null;
        }
        return binRegistration.bin.id.lt(lastBinId);
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
