package apartments.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationResponse {
    String provinceName;
    String districtName;
    String wardName;
    String provinceCode;
    String districtCode;
    String wardCode;
    String street;
    String fullAddress;
}
