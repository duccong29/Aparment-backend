package apartments.mapper;

import apartments.dto.request.poster.PosterRequest;
import apartments.dto.response.PosterResponse;
import apartments.entity.Poster;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface PosterMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "approvedBy.id", target = "approveId")
    PosterResponse toPosterResponse(Poster poster);

    @Mapping(target = "user.id", source = "userId")
    Poster toPoster(PosterRequest request);



}

