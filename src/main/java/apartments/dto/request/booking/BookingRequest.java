package apartments.dto.request.booking;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequest {
    LocalDate checkinDate;
    LocalDate checkoutDate;
    Integer guests;
    String note;
    String ipAddress;
}
