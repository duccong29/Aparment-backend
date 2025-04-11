package apartments.mapper;

import apartments.dto.request.comment.CommentCreationRequest;
import apartments.dto.response.CommentResponse;
import apartments.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "apartment.id", source = "apartmentId")
    Comment toComment(CommentCreationRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "apartmentId", source = "apartment.id")
    CommentResponse toCommentResponse(Comment comment);

    List<CommentResponse> toCommentResponses(List<Comment> comments);

}

