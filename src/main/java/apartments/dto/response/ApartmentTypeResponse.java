package apartments.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApartmentTypeResponse {
    String id;
    String name;
    String status;
    String userName;
    Instant createdDate;
    Instant modifiedDate;
}
