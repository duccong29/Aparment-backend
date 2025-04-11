package apartments.controller;

import apartments.dto.request.payment.InitPaymentRequest;
import apartments.dto.request.payment.InitPaymentResponse;
import apartments.entity.Booking;
import apartments.service.ApartmentService;
import apartments.service.BookingService;
import apartments.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {
    PaymentService paymentService;
    BookingService bookingService;
    ApartmentService apartmentService;

    @PostMapping("/init")
    public InitPaymentResponse initPayment(@RequestBody InitPaymentRequest request, HttpServletRequest httpRequest) {
        String ipAddress = PaymentService.getIpAddress(httpRequest);
        request.setIpAddress(ipAddress);
        return paymentService.init(request);
    }

    @GetMapping("/return")
    public String handleVNPayReturn(@RequestParam Map<String, String> allParams) {
        log.info("VNPay return with params: {}", allParams);

        boolean isValid = paymentService.verifyIpn(allParams);
        if (!isValid) {
            return "Chữ ký không hợp lệ! (Invalid signature)";
        }

        String bookingCode = allParams.get("vnp_TxnRef");
        String responseCode = allParams.get("vnp_ResponseCode");
        Booking booking = bookingService.findBookingByBookingCode(bookingCode);
        String apartmentId = booking.getApartment().getId();

        if ("00".equals(responseCode)) {
            bookingService.markAsBooked(bookingCode);
            apartmentService.updateApartmentStatusToBooked(apartmentId);
            return "Thanh toán thành công! 🎉";
        } else {
            bookingService.markAsFailed(bookingCode);
            return "Thanh toán thất bại! ❌ Mã lỗi: " + responseCode;
        }
    }
}
