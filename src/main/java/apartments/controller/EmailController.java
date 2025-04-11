package apartments.controller;

import apartments.dto.response.ApiResponse;
import apartments.dto.response.UserConfirmResponse;
import apartments.service.AuthenticationService;
import apartments.service.EmailService;
import event.dto.NotificationEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {
    AuthenticationService authenticationService;
    EmailService emailService;

    @GetMapping("/confirm")
    ApiResponse<UserConfirmResponse> confirmEmail(@RequestParam("token") String token) {
        return ApiResponse.<UserConfirmResponse>builder()
                .data(authenticationService.activateAccount(token))
                .build();
    }

    @KafkaListener(topics = "email-topic")
    public void consumeEmailMessage(NotificationEvent message) {
        try {
            if (!"EMAIL".equalsIgnoreCase(message.getChannel())) return;

            Map<String, Object> param = message.getParam();
            if (param == null || !param.containsKey("token")) {
                log.warn("Không tìm thấy token trong param để gửi email xác nhận");
                return;
            }

            String token = (String) param.get("token");
            emailService.sendConfirmationEmail(message.getRecipient(), token);

        } catch (Exception e) {
            log.error("Gửi email xác nhận thất bại: {}", e.getMessage(), e);
        }
    }
}
