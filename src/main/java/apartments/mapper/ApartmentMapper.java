package apartments.mapper;


import apartments.dto.response.ApartmentResponse;
import apartments.entity.Apartment;
import apartments.dto.request.apartment.ApartmentRequest;
import apartments.entity.ApartmentDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface ApartmentMapper {
    @Mapping(target = "images", ignore = true)
    Apartment toApartment(ApartmentRequest request);

    @Mapping(source = "apartmentType.name", target = "apartmentTypeName")
    @Mapping(source = "user.userName", target = "userName")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "location", source = "location")
    ApartmentResponse toApartmentResponse(Apartment apartment);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "location", ignore = true)
    void updateApartment(ApartmentRequest request, @MappingTarget Apartment apartment);

    @Mapping(target = "userName", source = "user.userName")
    @Mapping(source = "apartmentType.name", target = "apartmentTypeName")
    @Mapping(target = "location", source = "location")
    @Mapping(source = "images", target = "images")
    ApartmentDocument toDocument(Apartment apartment);

}
