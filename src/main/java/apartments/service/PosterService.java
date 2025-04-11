package apartments.service;

import apartments.constant.PredefinedRole;
import apartments.constant.PredefinedStatus;
import apartments.dao.PosterRepository;
import apartments.dao.RoleRepository;
import apartments.dao.UserRepository;
import apartments.dto.request.poster.AdminActionRequest;
import apartments.dto.request.poster.ApprovePosterRequest;
import apartments.dto.response.PosterResponse;
import apartments.entity.Poster;
import apartments.entity.Role;
import apartments.entity.User;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.mapper.PosterMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PosterService {
    PosterRepository posterRepository;
    PosterMapper posterMapper;
    UserRepository userRepository;
    RoleRepository roleRepository;
    EmailService emailService;
    AuthenticationService authenticationService;

    @PreAuthorize("hasRole('USER')")
    @Transactional
    public PosterResponse createPoster() {

        String currentUserId = authenticationService.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(ErrorCodes.USERS_NOT_FOUND));

        if (!user.isEnabled()) {
            throw new AppException(ErrorCodes.EMAIL_NOT_CONFIRMED);
        }

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals(PredefinedRole.POSTER_ROLE))) {
            throw new AppException(ErrorCodes.USER_ALREADY_POSTER);
        }

        boolean hasPendingRequest = posterRepository.existsByUserIdAndStatus(currentUserId, PredefinedStatus.POSTER_PENDING);
        if (hasPendingRequest) {
            throw new AppException(ErrorCodes.POSTER_REQUEST_PENDING);
        }

        Poster newPoster = new Poster();
        newPoster.setStatus(PredefinedStatus.POSTER_PENDING);
        newPoster.setUser(user);

        Poster savedPoster = posterRepository.save(newPoster);
        log.info("User {} created poster request: {}", currentUserId, savedPoster.getId());

        return posterMapper.toPosterResponse(savedPoster);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<PosterResponse> getAllPosters() {
        return posterRepository.findAll()
                .stream()
                .map(posterMapper::toPosterResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<PosterResponse> getPostersByStatus(String status) {
        if (!isValidStatus(status)) {
            throw new AppException(ErrorCodes.INVALID_STATUS);
        }
        String lowerCaseStatus = status.toLowerCase();

        return posterRepository.findAll()
                .stream()
                .filter(poster -> poster.getStatus().toLowerCase().equals(lowerCaseStatus))
                .map(posterMapper::toPosterResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PosterResponse getPosterById(String id) {
        Poster poster = posterRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCodes.POSTER_NOT_FOUND));
        return posterMapper.toPosterResponse(poster);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public PosterResponse approvePoster(String posterId, ApprovePosterRequest request) {
        String adminId = authenticationService.getCurrentUserId();

        User approveId = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCodes.USERS_NOT_FOUND));

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new AppException(ErrorCodes.POSTER_NOT_FOUND));

        if (!poster.getStatus().equals(PredefinedStatus.POSTER_PENDING)) {
            throw new AppException(ErrorCodes.INVALID_POSTER_STATUS);
        }
        User posterUser = poster.getUser();

        if (request.isApproved()) {
            poster.setStatus(PredefinedStatus.POSTER_APPROVED);

            if (posterUser.getRoles().stream()
                    .noneMatch(role -> role.getName().equals(PredefinedRole.POSTER_ROLE))) {
                Role posterRole = roleRepository.findByName(PredefinedRole.POSTER_ROLE)
                        .orElseThrow(() -> new AppException(ErrorCodes.ROLE_NOT_FOUND));
                posterUser.getRoles().add(posterRole);
                userRepository.save(posterUser);

                emailService.sendPosterApprovalEmail(
                        posterUser.getEmail(),
                        posterUser.getUserName()
                );
                log.info("Admin {} approved poster for user {}", adminId, posterUser.getId());
            }
        } else {
            poster.setStatus(PredefinedStatus.POSTER_REJECTED);
            log.info("Admin {} rejected poster {} for user {}", adminId, posterId, posterUser.getId());
        }

        poster.setAdminNote(request.getAdminNote());
        poster.setApprovedBy(approveId);
        poster.setApprovalDate(Instant.now());

        poster = posterRepository.save(poster);

        return posterMapper.toPosterResponse(poster);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PosterResponse manualApproveUser(String posterId, AdminActionRequest request) {
        String adminId = authenticationService.getCurrentUserId();

        // Lấy bản ghi poster đang chờ duyệt
        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new AppException(ErrorCodes.POSTER_NOT_FOUND));

        if (!poster.getStatus().equals(PredefinedStatus.POSTER_REJECTED)) {
            throw new AppException(ErrorCodes.INVALID_POSTER_STATUS);
        }

        // Lấy thông tin user của poster đó
        User user = poster.getUser();

        // Kiểm tra nếu đã có quyền POSTER
        boolean alreadyPoster = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(PredefinedRole.POSTER_ROLE));

        if (alreadyPoster) {
            throw new AppException(ErrorCodes.USER_ALREADY_POSTER);
        }

        // Lấy thông tin admin hiện tại
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCodes.USERS_NOT_FOUND));

        // Thêm quyền POSTER cho user
        Role posterRole = roleRepository.findByName(PredefinedRole.POSTER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCodes.ROLE_NOT_FOUND));
        user.getRoles().add(posterRole);

        // Cập nhật trạng thái của poster
        poster.setStatus(PredefinedStatus.POSTER_APPROVED);
        poster.setApprovedBy(admin);
        poster.setAdminNote(request.getAdminNote());
        poster.setApprovalDate(Instant.now());
        // Lưu lại
        userRepository.save(user);
        posterRepository.save(poster);

        // Gửi email
        emailService.sendPosterApprovalEmail(user.getEmail(), user.getUserName());
        log.info("Admin manually approved POSTER role for posterId {}, userId {}", posterId, user.getId());

        return posterMapper.toPosterResponse(poster);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public PosterResponse revokePosterRole(String posterId, AdminActionRequest request) {
        String adminId = authenticationService.getCurrentUserId();

        Poster poster = posterRepository.findById(posterId)
                .orElseThrow(() -> new AppException(ErrorCodes.POSTER_NOT_FOUND));

        User approveId = userRepository.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCodes.USERS_NOT_FOUND));

        if (!poster.getStatus().equals(PredefinedStatus.POSTER_APPROVED)) {
            throw new AppException(ErrorCodes.INVALID_POSTER_STATUS);
        }

        User user = poster.getUser();
        boolean removed = user.getRoles().removeIf(role ->
                role.getName().equals(PredefinedRole.POSTER_ROLE));

        if (!removed) {
            throw new AppException(ErrorCodes.USER_NOT_POSTER);
        }

        poster.setStatus(PredefinedStatus.POSTER_REVOKED);
        poster.setApprovedBy(approveId);
        poster.setAdminNote(request.getAdminNote());
        poster.setApprovalDate(Instant.now());
        posterRepository.save(poster);
        userRepository.save(user);

        emailService.sendPosterRevocationEmail(
                user.getEmail(),
                user.getUserName()
        );
        log.info("Admin revoked POSTER role from user {}", posterId);
        return posterMapper.toPosterResponse(poster);
    }

    private boolean isValidStatus(String status) {
        String lowerCaseStatus = status.toLowerCase();
        return PredefinedStatus.POSTER_PENDING.toLowerCase().equals(lowerCaseStatus) ||
                PredefinedStatus.POSTER_APPROVED.toLowerCase().equals(lowerCaseStatus) ||
                PredefinedStatus.POSTER_REJECTED.toLowerCase().equals(lowerCaseStatus) ||
                PredefinedStatus.POSTER_REVOKED.toLowerCase().equals(lowerCaseStatus);
    }
}
