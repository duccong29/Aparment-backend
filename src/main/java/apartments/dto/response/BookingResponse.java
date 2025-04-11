package apartments.dto.response;

import apartments.dto.request.payment.InitPaymentResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponse {
    String id;
    String bookingCode;
    LocalDate checkinDate;
    LocalDate checkoutDate;
    Integer guests;
    String status;
    BigDecimal subtotal;
    BigDecimal discount;
    BigDecimal totalAmount;
    String note;
    Instant createdAt;
    Instant updatedAt;
    String apartmentId;
    String userId;

    InitPaymentResponse payment;
}
