package apartments.controller;

import apartments.dto.request.booking.BookingRequest;
import apartments.dto.response.BookingResponse;
import apartments.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;

        @PostMapping("/apartment/{apartmentId}")
        public ResponseEntity<BookingResponse> createBooking(
                @PathVariable String apartmentId,
                @RequestBody BookingRequest request,
                HttpServletRequest httpRequest
        ) {
            BookingResponse response = bookingService.createBooking(apartmentId, request, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

//    @GetMapping
//    public ResponseEntity<List<BookingResponse>> getMyBookings() {
//        List<BookingResponse> bookings = bookingService.getBookingsForCurrentUser();
//        return ResponseEntity.ok(bookings);
//    }
//
//    @GetMapping("/{bookingCode}")
//    public ResponseEntity<BookingResponse> getBookingByCode(@PathVariable String bookingCode) {
//        BookingResponse response = bookingService.getBookingByCode(bookingCode);
//        return ResponseEntity.ok(response);
//    }
//
//    @DeleteMapping("/{bookingCode}")
//    public ResponseEntity<Void> cancelBooking(@PathVariable String bookingCode) {
//        bookingService.cancelBooking(bookingCode);
//        return ResponseEntity.noContent().build();
//    }
}
