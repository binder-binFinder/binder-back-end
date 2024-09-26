package net.binder.api.notification.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import net.binder.api.bin.entity.Bin;
import net.binder.api.bin.entity.BinType;
import net.binder.api.binregistration.entity.BinRegistration;
import net.binder.api.notification.entity.Notification;
import net.binder.api.notification.entity.NotificationType;

@Getter
public class NotificationDetail {

    private final Long notificationId;

    private final String binTitle;

    private final String binAddress;

    private final BinType binType;

    private final NotificationType notificationType;

    private final String reasonMessage;

    private final Boolean isRead;

    private final Boolean isBinCreator;

    private final LocalDateTime createdAt;

    @Builder
    public NotificationDetail(Long notificationId, String binTitle, String binAddress, BinType binType,
                              NotificationType notificationType,
                              String reasonMessage, Boolean isRead, Boolean isBinCreator, LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.binTitle = binTitle;
        this.binAddress = binAddress;
        this.binType = binType;
        this.notificationType = notificationType;
        this.reasonMessage = reasonMessage;
        this.isRead = isRead;
        this.isBinCreator = isBinCreator;
        this.createdAt = createdAt;
    }

    public static NotificationDetail from(Notification notification) {
        Bin bin = notification.getBin();

        return NotificationDetail.builder()
                .notificationId(notification.getId())
                .binTitle(bin.getTitle())
                .binAddress(bin.getAddress())
                .binType(bin.getType())
                .notificationType(notification.getType())
                .reasonMessage(notification.getAdditionalInfo())
                .isRead(notification.isRead())
                .isBinCreator(isBinCreator(bin, notification))
                .createdAt(notification.getCreatedAt())
                .build();
    }

    // 알림 수신자가 쓰레기통 주인과 같을 경우 true
    private static boolean isBinCreator(Bin bin, Notification notification) {

        BinRegistration binRegistration = bin.getBinRegistration();
        if (binRegistration == null) {
            return false;
        }
        return notification.getReceiver().equals(binRegistration.getMember());
    }
}
