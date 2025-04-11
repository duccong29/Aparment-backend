package apartments.constant;

public class PredefinedStatus {
    public static final String USER_PENDING = "PENDING";
    public static final String USER_ACTIVE = "ACTIVE";
    public static final String USER_BANNED = "BANNED";
    public static final String USER_INACTIVE = "INACTIVE";

    public static final String POSTER_PENDING = "PENDING";
    public static final String POSTER_APPROVED = "APPROVED";
    public static final String POSTER_REJECTED = "REJECTED";
    public static final String POSTER_REVOKED = "REVOKED";

    public static final String BOOKING_PENDING = "PENDING";          // Chờ xác nhận
    public static final String BOOKING_CONFIRMED = "CONFIRMED";      // Đã xác nhận (sau khi thanh toán hoặc duyệt)
    public static final String BOOKING_CANCELLED = "CANCELLED";      // Bị hủy bởi người dùng hoặc admin
    public static final String BOOKING_COMPLETED = "COMPLETED";      // Đã hoàn thành sau khi checkout
    public static final String BOOKING_EXPIRED = "EXPIRED";          // Hết hạn (quá thời gian chưa thanh toán)

    public static final String APARTMENT_AVAILABLE = "AVAILABLE";    // Căn hộ có sẵn cho thuê
    public static final String APARTMENT_BOOKED = "BOOKED";          // Căn hộ đã được đặt
    public static final String APARTMENT_UNAVAILABLE = "UNAVAILABLE"; // Căn hộ không thể cho thuê
    public static final String APARTMENT_SOLD = "SOLD";              // Căn hộ đã được bán
    public static final String APARTMENT_UNDER_REPAIR = "UNDER_REPAIR"; // Căn hộ đang sửa chữa
    public static final String APARTMENT_BLOCKED = "BLOCKED";        // Căn hộ bị khóa tạm thời

    private PredefinedStatus() {}
}
