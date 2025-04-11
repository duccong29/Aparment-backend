package apartments.mapper;

import apartments.dto.request.apartment.ApartmentRequest;
import apartments.dto.response.ApartmentResponse;
import apartments.entity.ApartmentDocument;
import org.mapstruct.Mapping;

public interface SearchApartmentMapper {
    @Mapping(target = "id", ignore = true)
    ApartmentDocument toSearchApartment(ApartmentRequest request);

    ApartmentResponse documentToResponse(ApartmentDocument document);
}
