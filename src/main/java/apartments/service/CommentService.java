package apartments.service;

import apartments.dao.ApartmentRepository;
import apartments.dao.CommentRepository;
import apartments.dto.request.comment.CommentCreationRequest;
import apartments.dto.request.notification.NotificationRequest;
import apartments.entity.Comment;
import apartments.dto.response.CommentResponse;
import apartments.mapper.CommentMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentMapper commentMapper;
    CommentRepository commentRepository;
    ApartmentRepository apartmentRepository;
    NotificationService notificationService;
    SimpMessagingTemplate messagingTemplate;

    /**
     * Lưu bình luận và gửi thông báo qua WebSocket.
     *
     * @param request thông tin chi tiết của bình luận
     * @return thông tin phản hồi của bình luận sau khi lưu
     */
    public CommentResponse saveComment(CommentCreationRequest request) {
        // Map từ request sang entity
        Comment comment = commentMapper.toComment(request);

        // Lưu bình luận vào cơ sở dữ liệu
        Comment savedComment = commentRepository.save(comment);

        // Map từ entity sang response
        CommentResponse response = commentMapper.toCommentResponse(savedComment);

        // Lấy thông tin ID của chủ căn hộ
        String posterUserId = apartmentRepository.findUserIdByApartmentId(comment.getApartment().getId());
        response.setPosterUserId(posterUserId);

        // Gửi bình luận qua WebSocket
        String websocketTopic = "/topic/comments/" + request.getApartmentId();
        messagingTemplate.convertAndSend(websocketTopic, response);
        log.info("Sent comment via WebSocket: topic={}, commentId={}, userId={}, apartmentId={}, content={}",
                websocketTopic, response.getId(), response.getUserId(), response.getApartmentId(), response.getContent());

        // Tạo thông báo và gửi qua NotificationService
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .recipientId(posterUserId)
                .apartmentId(response.getApartmentId())
                .commentId(response.getId())
                .message("Bạn có một bình luận mới từ " + comment.getUser().getId() + " trên căn hộ: " + response.getApartmentId())
                .build();

        notificationService.sendNotification(notificationRequest);

        return response;
    }

    public List<CommentResponse> getComments(String apartmentId) {
        List<Comment> comments = commentRepository.findByApartmentId(apartmentId);
        return comments.stream()
                .map(commentMapper::toCommentResponse)
                .toList();
    }
}
