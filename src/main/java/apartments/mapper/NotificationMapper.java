package apartments.mapper;

import apartments.dto.request.notification.NotificationRequest;
import apartments.dto.response.NotificationResponse;
import apartments.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "recipient.id", source = "recipientId")
    Notification toNotification(NotificationRequest request);

    @Mapping(target = "recipientId", source = "recipient.id") // Lấy recipientId từ recipient
    @Mapping(target = "recipientName", source = "recipient.userName") // Lấy recipientName từ recipient
    NotificationResponse toNotificationResponse(Notification notification);
}
