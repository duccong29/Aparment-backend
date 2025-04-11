package apartments.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApartmentResponse {
    String id;
    String title;
    String description;
    Double price;
    Double area;
    String status;
    String apartmentTypeName;
    List<ImageResponse> images;

    LocationResponse location;

    String userName;
    String userId;
    Instant createdDate;
    Instant modifiedDate;
}
