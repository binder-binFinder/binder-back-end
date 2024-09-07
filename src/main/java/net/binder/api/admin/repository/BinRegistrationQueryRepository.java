package net.binder.api.admin.repository;

import static net.binder.api.admin.dto.RegistrationFilter.ENTIRE;
import static net.binder.api.binregistration.entity.QBinRegistration.binRegistration;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.admin.dto.RegistrationFilter;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.binregistration.entity.BinRegistrationStatus;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BinRegistrationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<BinRegistration> findAll(RegistrationFilter filter) {

        BooleanBuilder booleanBuilder = getBooleanBuilder(filter);

        return jpaQueryFactory.selectFrom(binRegistration)
                .join(binRegistration.bin).fetchJoin()
                .join(binRegistration.member).fetchJoin()
                .where(booleanBuilder)
                .orderBy(binRegistration.createdAt.desc())
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
}
