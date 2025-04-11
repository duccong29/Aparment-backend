package apartments.mapper;

import apartments.dto.request.apartment.ApartmentTypeRequest;
import apartments.dto.response.ApartmentTypeResponse;
import apartments.entity.ApartmentType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ApartmentTypeMapper {
    ApartmentType toApartmentType(ApartmentTypeRequest request);

    ApartmentTypeResponse toApartmentTypeResponse(ApartmentType apartmentType);

//    @Mapping(target = "id", ignore = true)
    void updateApartmentType(ApartmentTypeRequest request, @MappingTarget ApartmentType apartmentType);

}
