package apartments.service;

import apartments.constant.PredefinedRole;
import apartments.dao.PosterRepository;
import apartments.dao.RoleRepository;
import apartments.dto.request.user.ForgotPasswordRequest;
import apartments.dto.response.ForgotPasswordResponse;
import apartments.entity.Role;
import apartments.entity.User;
import apartments.mapper.PosterMapper;
import event.dto.NotificationEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import apartments.dao.UserRepository;
import apartments.dto.request.user.UserCreationRequest;
import apartments.dto.request.user.UserUpdateRequest;
import apartments.dto.response.UserResponse;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.mapper.UserMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    EmailService emailService;
    AuthenticationService authenticationService;
    KafkaTemplate<String, Object> kafkaTemplate;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCodes.EMAIL_ALREADY_EXISTS);
        }
        User user = userMapper.toUser(request);
        user.setPassWord(passwordEncoder.encode(request.getPassWord()));

        String email = user.getEmail();
        String generatedUsername = email.substring(0, email.indexOf("@"));
        user.setUserName(generatedUsername);

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        String token = authenticationService.generateActivationToken(user);

        try {
             userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCodes.UNCATEGORIZED_EXCEPTION);
        }
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(request.getEmail())
                .subject("CONFIRM_EMAIL")
                .body("Hello, " + request.getUserName())
                .param(Map.of("token", token))
                .build();
        kafkaTemplate.send("email-topic", notificationEvent);

//        emailService.sendConfirmationEmail(user.getEmail(), token);
        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }


    //@PostAuthorize("returnObject.userName == authentication.name")
    //@PreAuthorize("#request.userName == authentication.principal.name or hasRole('ADMIN')")
    //@PreAuthorize("#user.id == #userId")
    //@PreAuthorize("#request.userName == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_EXISTED));
        userMapper.updateUser(user, request);
        user.setPassWord(passwordEncoder.encode(request.getPassWord()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> role.getName().equals("ADMIN")))
                .map(userMapper::toUserResponse)
                .toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String userId) {
        return userMapper.toUserResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_EXISTED)));

    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCodes.EMAIL_ALREADY_EXISTS));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCodes.EMAIL_NOT_CONFIRMED);
        }
        String resetPasswordToken = authenticationService.generateResetPasswordToken(user);
        log.info("forgotPassword: {}", resetPasswordToken);
        userRepository.save(user);

        emailService.sendForgotPasswordEmail(user.getEmail(), resetPasswordToken);

        return ForgotPasswordResponse.builder()
                .status(true)
                .message("Password reset email sent successfully.")
                .build();
    }

    public String getUserNameById(String userId) {
        return userRepository.findById(userId)
                .map(User::getUserName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
