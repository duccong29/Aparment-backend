package apartments.mapper;

import apartments.dto.request.apartment.LocationRequest;
import apartments.dto.response.LocationResponse;
import apartments.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fullAddress", ignore = true)
    Location toEntity(LocationRequest request);

    @Mapping(target = "fullAddress", expression = "java(generateFullAddress(location))")
    LocationResponse toResponse(Location location);

    default String generateFullAddress(Location location) {
        return String.format("%s, %s, %s, %s",
                location.getStreet(),
                location.getWardName(),
                location.getDistrictName(),
                location.getProvinceName());
    }
}
