package apartments.mapper;

import apartments.dto.request.apartment.ImageRequest;
import apartments.dto.response.ImageResponse;
import apartments.entity.Image;
import apartments.entity.ImageDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {
    Image toImage(ImageRequest request);

    ImageResponse toImageResponse(Image image);

    ImageDocument toImageDocument(Image image);
}
