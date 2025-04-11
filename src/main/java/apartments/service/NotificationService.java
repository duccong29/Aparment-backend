package apartments.service;

import apartments.dao.NotificationRepository;
import apartments.dto.request.notification.NotificationRequest;
import apartments.dto.response.NotificationResponse;
import apartments.entity.Notification;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.mapper.NotificationMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo qua WebSocket và lưu thông báo vào cơ sở dữ liệu.
     *
     * @param request thông tin chi tiết của thông báo
     */
    public void sendNotification(NotificationRequest request) {
        // Map từ request sang entity
        Notification notification = notificationMapper.toNotification(request);

        // Lưu thông báo vào cơ sở dữ liệu
        notification = notificationRepository.save(notification);
        notification.getApartmentId();
        notification.getCommentId();
        // Map từ entity sang response
        NotificationResponse response = notificationMapper.toNotificationResponse(notification);

        // Gửi thông báo qua WebSocket
        String websocketTopic = "/topic/notifications/" + notification.getRecipient().getId();
        messagingTemplate.convertAndSend(websocketTopic, response);
        log.info("Sent notification via WebSocket: topic={}, recipientId={}, message={}",
                websocketTopic, notification.getRecipient().getId(), notification.getMessage());

    }

    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCodes.NOTIFICATION_NOT_FOUND));
        notification.setReadStatus(true);
        notificationRepository.save(notification);

        Notification updatedNotification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCodes.NOTIFICATION_NOT_FOUND));
        return notificationMapper.toNotificationResponse(updatedNotification);
    }

    public List<NotificationResponse> getNotificationsByRecipientId(String recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientId(recipientId);
        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }
}
