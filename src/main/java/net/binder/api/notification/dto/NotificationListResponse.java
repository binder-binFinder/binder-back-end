package net.binder.api.notification.dto;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NotificationListResponse {

    private final List<NotificationDetail> notificationDetails;
}
