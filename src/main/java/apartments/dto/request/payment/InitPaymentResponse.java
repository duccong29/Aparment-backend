package apartments.dto.request.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitPaymentResponse {
    String vnpUrl;
}