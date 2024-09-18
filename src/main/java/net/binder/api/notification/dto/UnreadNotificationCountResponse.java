package net.binder.api.notification.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UnreadNotificationCountResponse {

    private final Long unreadCount;
}
