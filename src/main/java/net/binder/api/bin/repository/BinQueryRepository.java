package net.binder.api.bin.repository;

import static net.binder.api.bin.entity.QBinRegistration.binRegistration;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.bin.entity.BinRegistration;
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

    private BooleanExpression binIdLt(Long lastBinId) {
        if (lastBinId == null) {
            return null;
        }
        return binRegistration.bin.id.lt(lastBinId);
    }


}
