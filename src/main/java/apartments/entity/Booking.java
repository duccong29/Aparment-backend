package apartments.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    // Mã booking (đồng thời cũng dùng làm txnRef, bookingCode,...) có thể dùng để liên kết với thanh toán VNPay
    String bookingCode;
    LocalDate checkinDate;
    LocalDate checkoutDate;
    Integer guests;
    String status;
    // Giá trị tính tiền: subtotal (số tiền ban đầu), discount (nếu có), tổng tiền phải thanh toán
    BigDecimal subtotal;
    BigDecimal discount;
    BigDecimal totalAmount;
    String note;
    Instant createdAt;
    Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
