package apartments.mapper;

import apartments.dto.request.booking.BookingRequest;
import apartments.dto.response.BookingResponse;
import apartments.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "apartment.id", target = "apartmentId")
    BookingResponse toBookingResponse(Booking booking);

    Booking toBooking(BookingRequest bookingRequest);
}
