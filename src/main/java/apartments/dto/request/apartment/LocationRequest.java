package apartments.dto.request.apartment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationRequest {
    String provinceCode;
    String districtCode;
    String wardCode;
    String street;
}
