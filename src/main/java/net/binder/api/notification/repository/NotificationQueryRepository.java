package net.binder.api.notification.repository;

import static net.binder.api.notification.entity.QNotification.notification;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.notification.entity.Notification;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;


    public List<Notification> findAllByMemberId(Long memberId, Long lastNotificationId, int pageSize) {

        return jpaQueryFactory
                .select(notification)
                .from(notification)
                .where(equalMemberId(memberId), lessThan(lastNotificationId))
                .orderBy(notification.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression equalMemberId(Long memberId) {
        return notification.receiver.id.eq(memberId);
    }

    private static BooleanExpression lessThan(Long lastNotificationId) {
        if (lastNotificationId == null) {
            return null;
        }
        return notification.id.lt(lastNotificationId);
    }
}
