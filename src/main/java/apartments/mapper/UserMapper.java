package apartments.mapper;

import apartments.dto.request.user.UserCreationRequest;
import apartments.dto.request.user.UserUpdateRequest;
import apartments.dto.response.UserResponse;
import apartments.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    //@Mapping(target = "roles", ignore = true)
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request );

}

