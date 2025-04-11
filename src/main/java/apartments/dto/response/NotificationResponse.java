package apartments.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    String message;
    String notifiedAt;
    boolean readStatus;
    String apartmentId;
    Long commentId;
    String recipientId;
    String recipientName;
}
