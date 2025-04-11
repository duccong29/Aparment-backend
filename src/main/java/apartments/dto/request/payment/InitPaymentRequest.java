package apartments.dto.request.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitPaymentRequest {
     String requestId;
     String ipAddress;
     String userId;
     String txnRef;
     long amount;

}