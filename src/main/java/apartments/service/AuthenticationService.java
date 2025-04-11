package apartments.service;

import apartments.constant.PredefinedRole;
import apartments.dao.httpClient.OutboundIdentityClient;
import apartments.dao.httpClient.OutboundUserClient;
import apartments.dto.response.ResetPasswordResponse;
import apartments.dto.response.UserConfirmResponse;
import apartments.entity.InvalidatedToken;
import apartments.entity.Role;
import apartments.entity.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import apartments.dao.InvalidatedTokenRepository;
import apartments.dao.UserRepository;
import apartments.dto.request.AuthenticationRequest;
import apartments.dto.request.ExchangeTokenRequest;
import apartments.dto.request.jwt.IntrospectRequest;
import apartments.dto.request.jwt.LogoutRequest;
import apartments.dto.request.jwt.RefreshRequest;
import apartments.dto.response.AuthenticationResponse;
import apartments.dto.response.IntrospectResponse;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    @NonFinal
    @Value("${spring.jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${spring.jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${spring.jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${spring.outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${spring.outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${spring.outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse outboundAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        log.info("TOKEN RESPONSE {}", response);

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("UserInfo {}", userInfo);

        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(PredefinedRole.USER_ROLE).build());

        var user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(userInfo.getEmail())
                        .userName(userInfo.getName())
                        .firstName(userInfo.getGivenName())
                        .lastName(userInfo.getFamilyName())
                        .roles(roles)
                        .build()));

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_EXISTED));
        if (!user.isEnabled()) {
            throw new AppException(ErrorCodes.EMAIL_NOT_CONFIRMED);
        }
        boolean authenticated = passwordEncoder.matches(request.getPassWord(), user.getPassWord());

        if (!authenticated) throw new AppException(ErrorCodes.UNAUTHENTICATED);

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var email = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCodes.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).build();
    }

    private String generateTokenUser(String subject, String issuer, String type) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.MINUTES).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", type)
                .build();

        try {
            JWSObject jwsObject = new JWSObject(header, new Payload(claims.toJSONObject()));
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    public UserConfirmResponse activateAccount(String token) {
        validateToken(token, "activation");
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String email = signedJWT.getJWTClaimsSet().getSubject();

            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_EXISTED));

            if (user.isEnabled()) {
                return UserConfirmResponse.builder()
                        .status(false)
                        .message("Account already activated.")
                        .expiresAt(null)
                        .build();
            }

            user.setEnabled(true);
            userRepository.save(user);

            return UserConfirmResponse.builder()
                    .status(true)
                    .message("Account activated successfully.")
                    .expiresAt(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();

        } catch (ParseException e) {
            log.error("Invalid token format", e);
            throw new AppException(ErrorCodes.INVALID_TOKEN);
        } catch (AppException e) {
            log.error("Activation failed", e);
            throw new AppException(ErrorCodes.INVALID_TOKEN);
        }
    }


    public ResetPasswordResponse resetPassword(String token, String newPassword) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        validateToken(token, "reset-password");
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String email = signedJWT.getJWTClaimsSet().getSubject();

            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_EXISTED));

            user.setPassWord(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return ResetPasswordResponse.builder()
                    .status(true)
                    .message("Password reset successfully.")
                    .expiresAt(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();
        } catch (Exception e) {
            log.error("Password reset failed", e);
            throw new AppException(ErrorCodes.INVALID_TOKEN);
        }
    }

    //login
    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("demo.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCodes.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCodes.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
            });
        return stringJoiner.toString();
    }

    public void validateToken(String token, String expectedType) {
        try {
            SignedJWT signedJWT = verifyToken(token, false);
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            if (!signedJWT.verify(verifier)) {
                throw new AppException(ErrorCodes.INVALID_TOKEN);
            }
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            if (new Date().after(claims.getExpirationTime())) {
                throw new AppException(ErrorCodes.EXPIRED_RESET_TOKEN);
            }
            String type = claims.getStringClaim("type");
            if (!expectedType.equals(type)) {
                throw new AppException(ErrorCodes.INVALID_TOKEN);
            }

            if (invalidatedTokenRepository.existsById(claims.getJWTID())) {
                throw new AppException(ErrorCodes.UNAUTHENTICATED);
            }
        } catch (Exception e) {
            log.error("Token validation failed", e);
            throw new AppException(ErrorCodes.INVALID_TOKEN);
        }
    }

    public String generateActivationToken(User user) {
        return generateTokenUser(user.getEmail(), "signup", "activation");
    }

    public String generateResetPasswordToken(User user) {
        return generateTokenUser(user.getEmail(), "forgot-password", "reset-password");
    }

    public String getCurrentUserId() {
        Authentication authentication = getAuthentication();
        return authentication.getName();
    }

    public boolean isAdmin() {
        Authentication authentication = getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + PredefinedRole.ADMIN_ROLE));
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCodes.UNAUTHORIZED);
        }
        return authentication;
    }
}
