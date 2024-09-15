package net.binder.api.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.common.annotation.CurrentUser;
import net.binder.api.notification.dto.NotificationDetail;
import net.binder.api.notification.dto.NotificationListResponse;
import net.binder.api.notification.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "알림 관리")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "마지막으로 조회한 알림 id를 기준으로 알림 목록을 최신순으로 조회할 수 있다.")
    @GetMapping
    public NotificationListResponse getNotificationList(@CurrentUser String email,
                                                        @RequestParam(required = false) Long lastId) {
        List<NotificationDetail> notificationDetails = notificationService.getNotificationDetails(email, lastId);
        Long unreadCount = notificationService.getUnreadCount(email);

        return new NotificationListResponse(notificationDetails, unreadCount);
    }
}
