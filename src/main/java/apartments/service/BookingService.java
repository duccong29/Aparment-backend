package apartments.service;

import apartments.constant.PredefinedStatus;
import apartments.dao.ApartmentRepository;
import apartments.dao.BookingRepository;
import apartments.dao.UserRepository;
import apartments.dto.request.booking.BookingRequest;
import apartments.dto.request.payment.InitPaymentRequest;
import apartments.dto.response.BookingResponse;
import apartments.dto.response.booking.BookingStatusResponse;
import apartments.entity.Apartment;
import apartments.entity.Booking;
import apartments.entity.User;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.mapper.BookingMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
    BookingRepository bookingRepository;
    ApartmentRepository apartmentRepository;
    UserRepository userRepository;
    BookingMapper bookingMapper;
    AuthenticationService authenticationService;
    PaymentService paymentService;

    @Transactional
    public BookingResponse createBooking(String apartmentId, BookingRequest request, HttpServletRequest httpRequest) {
        validateRequest(request);
        validateApartment(apartmentId);
        String userId = authenticationService.getCurrentUserId();

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCodes.USERS_NOT_FOUND));

        Booking booking = bookingMapper.toBooking(request);
        booking.setBookingCode("BK" + System.currentTimeMillis());
        booking.setApartment(apartment);
        booking.setUser(user);
        booking.setStatus(PredefinedStatus.BOOKING_PENDING);
        booking.setTotalAmount(BigDecimal.valueOf(apartment.getPrice()));

        Booking savedBooking = bookingRepository.save(booking);

        String ipAddress = PaymentService.getIpAddress(httpRequest);
        InitPaymentRequest paymentRequest = InitPaymentRequest.builder()
                .userId(booking.getUser().getId())
                .amount(booking.getTotalAmount().longValue())
                .txnRef(savedBooking.getBookingCode())
                .requestId(savedBooking.getId())
                .ipAddress(ipAddress)
                .build();

        var paymentResponse  = paymentService.init(paymentRequest);

        log.info("Created booking with bookingCode: {}", booking.getBookingCode());
        BookingResponse response = bookingMapper.toBookingResponse(savedBooking);
        response.setPayment(paymentResponse);

        return response;
    }

    @Transactional
    public void markAsBooked(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ErrorCodes.BOOKING_NOT_FOUND));

        booking.setStatus(PredefinedStatus.BOOKING_CONFIRMED);
        booking.setUpdatedAt(Instant.now());

        bookingRepository.save(booking);
    }
    @Transactional
    public void markAsFailed(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ErrorCodes.BOOKING_NOT_FOUND));

        booking.setStatus(PredefinedStatus.BOOKING_CANCELLED);
        booking.setUpdatedAt(Instant.now());

        bookingRepository.save(booking);
    }
    public BookingStatusResponse getBookingStatus(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ErrorCodes.BOOKING_NOT_FOUND));

        return BookingStatusResponse.builder()
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus())
                .build();
    }

    private void validateRequest(BookingRequest request) {
        LocalDate checkinDate = request.getCheckinDate();
        LocalDate checkoutDate = request.getCheckoutDate();
        LocalDate currentDate = LocalDate.now();

        if (checkinDate.isBefore(currentDate)) {
            throw new AppException(ErrorCodes.CHECKIN_DATE_INVALID);
        }

        if (checkinDate.isAfter(checkoutDate)) {
            throw new AppException(ErrorCodes.CHECKOUT_DATE_INVALID);
        }

        if (request.getGuests() <= 0) {
            throw new AppException(ErrorCodes.GUESTS_INVALID);
        }
    }

    @Scheduled(cron = "0 03 23 * * ?")
    @Transactional
    public void updateCompletedBookings() {
        LocalDate currentDate = LocalDate.now();
        List<Booking> completedBookings = bookingRepository.findByStatusAndCheckoutDateBefore(
                PredefinedStatus.BOOKING_CONFIRMED,
                currentDate
        );

        for (Booking booking : completedBookings) {
            // Cập nhật trạng thái Booking
            booking.setStatus(PredefinedStatus.BOOKING_COMPLETED);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);

            // Cập nhật trạng thái Apartment
            Apartment apartment = booking.getApartment();
            apartment.setStatus(PredefinedStatus.APARTMENT_AVAILABLE);
            apartmentRepository.save(apartment);

            log.info("Booking {} completed. Apartment {} is now available.",
                    booking.getBookingCode(), apartment.getId());
        }
    }


    private void validateApartment(String apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));

        if (!PredefinedStatus.APARTMENT_AVAILABLE.equals(apartment.getStatus())) {
            throw new AppException(ErrorCodes.APARTMENT_NOT_AVAILABLE);
        }
    }

    public Booking findBookingByBookingCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ErrorCodes.BOOKING_NOT_FOUND));
    }
}
