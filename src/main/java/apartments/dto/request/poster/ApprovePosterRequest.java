package apartments.dto.request.poster;

import apartments.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApprovePosterRequest {
    String adminNote;
    boolean approved;
}
