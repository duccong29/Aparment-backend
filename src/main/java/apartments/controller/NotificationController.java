package apartments.controller;

import apartments.dto.request.notification.NotificationRequest;
import apartments.dto.response.ApiResponse;
import apartments.dto.response.NotificationResponse;
import apartments.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    @MessageMapping("/notifications/{userId}") // Endpoint cho WebSocket: /app/notifications
    public void handleNotification(@Payload NotificationRequest request) {
        log.info("Received notification request via WebSocket: recipientId={}, message={}",
                request.getRecipientId(), request.getMessage());
    }

    @PatchMapping("/notifications/{notificationId}")
    public ApiResponse<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        log.info("Marking notification with ID={} as read", notificationId);
        NotificationResponse response = notificationService.markAsRead(notificationId);
        return ApiResponse.<NotificationResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/notifications/{recipientId}")
    public ApiResponse<List<NotificationResponse>> getNotifications(@PathVariable("recipientId") String recipientId) {
        try {
            List<NotificationResponse> notifications = notificationService.getNotificationsByRecipientId(recipientId);
            return ApiResponse.<List<NotificationResponse>>builder()
                    .data(notifications)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching notification for recipientId {}: {}", recipientId, e.getMessage());
            return ApiResponse.<List<NotificationResponse>>builder()
                    .message("Unable to fetch notification: " + e.getMessage())
                    .build();
        }
    }




//    @KafkaListener(topics = "notification-delivery")
//    public void listenNotificationDelivery(NotificationEvent message) {
//        try {
//            emailService.sendEmail(
//                    message.getRecipient(),
//                    message.getChannel(),
//                    message.getBody()
//            );
//            log.info("Message received: {}", message);
//        } catch (Exception e){
//            log.error("Failed to send email to {}: {}", message.getRecipient(), e.getMessage());
//        }
//    }
//
//    @KafkaListener(topics = "comments")
//    public void consume(String message) {
//        log.info("Received: {}", message);
//    }
}
