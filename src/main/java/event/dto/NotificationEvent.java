package event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationEvent {
    String channel; // Kênh thông báo: "EMAIL", "SMS", "WEBSOCKET"
    String recipient; // Người nhận (email, số điện thoại, hoặc user ID)
    String templateCode; // Mã template (nếu cần gửi theo mẫu)
    Map<String, Object> param; // Các tham số động cho template
    String subject; // Tiêu đề (cho email hoặc thông báo chi tiết)
    String body; // Nội dung thông báo

}
