package net.binder.api.notification.dto;

import lombok.Builder;
import lombok.Getter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;

@Getter
public class NotificationDetail {

    private final Long notificationId;

    private final String binTitle;

    private final String binAddress;

    private final NotificationType notificationType;

    private final String reasonMessage;

    private final Boolean isRead;

    @Builder
    public NotificationDetail(Long notificationId, String binTitle, String binAddress,
                              NotificationType notificationType,
                              String reasonMessage, Boolean isRead) {
        this.notificationId = notificationId;
        this.binTitle = binTitle;
        this.binAddress = binAddress;
        this.notificationType = notificationType;
        this.reasonMessage = reasonMessage;
        this.isRead = isRead;
    }

    public static NotificationDetail from(Notification notification) {
        Bin bin = notification.getBin();

        return NotificationDetail.builder()
                .notificationId(notification.getId())
                .binTitle(bin.getTitle())
                .binAddress(bin.getAddress())
                .notificationType(notification.getType())
                .reasonMessage(notification.getAdditionalInfo())
                .isRead(notification.isRead())
                .build();
    }
}
