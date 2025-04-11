package apartments.controller;

import apartments.dto.request.comment.CommentCreationRequest;
import apartments.dto.response.ApiResponse;
import apartments.dto.response.CommentResponse;
import apartments.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;

    @MessageMapping("/comments/{apartmentId}")
    public CommentResponse handleComment(@Payload CommentCreationRequest request) {
        log.info("Received comment via WebSocket: apartmentId={}, userId={}, content={}",
                request.getApartmentId(), request.getUserId(), request.getContent());
        return commentService.saveComment(request);
    }

    @GetMapping("/comments/{apartmentId}")
    public ApiResponse<List<CommentResponse>> getComment(@PathVariable("apartmentId") String apartmentId) {
        try {
            List<CommentResponse> comments = commentService.getComments(apartmentId);
            return ApiResponse.<List<CommentResponse>>builder()
                    .data(comments)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching comments for apartmentId {}: {}", apartmentId, e.getMessage());
            return ApiResponse.<List<CommentResponse>>builder()
                    .message("Unable to fetch comments: " + e.getMessage())
                    .build();
        }
    }
}
