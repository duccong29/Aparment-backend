package apartments.dao;

import apartments.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,String> {
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByStatusAndCheckoutDateBefore(String status, LocalDate checkoutDate);

}
