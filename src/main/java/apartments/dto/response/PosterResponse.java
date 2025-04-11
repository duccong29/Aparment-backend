package apartments.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PosterResponse {
    String id;
    String userId;
    String status;
    String adminNote;
    String approveId;
    Instant createdAt;
    Instant approvalDate;
}
