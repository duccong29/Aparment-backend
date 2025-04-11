package apartments.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    JavaMailSender emailSender;
    @NonFinal
    @Value("${spring.mail.username}")
    protected String fromEmailId;

    public void sendEmail(String to, String subject, String text, boolean isHtml) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmailId);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, isHtml); // true = isHtml

            emailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendConfirmationEmail(String to, String token) {
        String subject = "Xác nhận email của bạn";
        String confirmationUrl = "http://localhost:3000/confirm?token=" + token;
        String text = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                        "<h2 style='color: #2E86C1;'>Xác nhận email</h2>" +
                        "<p>Vui lòng nhấp vào nút bên dưới để xác nhận email của bạn:</p>" +
                        "<a href='%s' style='display: inline-block; padding: 10px 20px; background-color: #2E86C1; color: white; text-decoration: none; border-radius: 5px;'>Xác nhận Email</a>" +
                        "<p style='margin-top: 20px;'>Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này.</p>" +
                        "</div>",
                confirmationUrl
        );
        sendEmail(to, subject, text, true);
    }

    public void sendForgotPasswordEmail(String to, String token) {
        String subject = "Đặt lại mật khẩu";
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        String text = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                        "<h2 style='color: #2E86C1;'>Đặt lại mật khẩu</h2>" +
                        "<p>Vui lòng nhấp vào nút bên dưới để đặt lại mật khẩu của bạn:</p>" +
                        "<a href='%s' style='display: inline-block; padding: 10px 20px; background-color: #2E86C1; color: white; text-decoration: none; border-radius: 5px;'>Đặt lại mật khẩu</a>" +
                        "<p style='margin-top: 20px;'>Liên kết này sẽ hết hạn sau 24 giờ.</p>" +
                        "</div>",
                resetUrl
        );
        sendEmail(to, subject, text, true);
    }

    public void sendPosterApprovalEmail(String to, String username) {
        String subject = "Thông báo: Yêu cầu trở thành Poster đã được duyệt";
        String text = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                        "<h2 style='color: #2E86C1;'>Xin chào %s!</h2>" +
                        "<p>Yêu cầu trở thành Poster của bạn đã được phê duyệt thành công.</p>" +
                        "<p>Bây giờ bạn có thể đăng bài và sử dụng các tính năng dành cho Poster.</p>" +
                        "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>" +
                        "<p>Trân trọng,<br>Đội ngũ quản trị hệ thống</p>" +
                        "</div>" +
                        "</div>",
                username
        );
        sendEmail(to, subject, text, true);
    }

    public void sendPosterRevocationEmail(String to, String username) {
        String subject = "Thông báo: Quyền Poster đã bị thu hồi";
        String text = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>"
                        + "<h2 style='color: #C0392B;'>Xin chào %s!</h2>"
                        + "<p>Chúng tôi xin thông báo rằng quyền Poster của bạn đã bị thu hồi bởi quản trị viên.</p>"
                        + "<p>Điều này đồng nghĩa với việc bạn sẽ không còn có thể đăng bài mới hoặc chỉnh sửa bài đăng cũ.</p>"
                        + "<p>Nếu bạn có thắc mắc hoặc cần hỗ trợ, vui lòng liên hệ với đội ngũ quản trị hệ thống.</p>"
                        + "<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>"
                        + "<p>Trân trọng,<br>Đội ngũ quản trị hệ thống</p>"
                        + "</div>"
                        + "</div>",
                username
        );
        sendEmail(to, subject, text, true);
    }

}
