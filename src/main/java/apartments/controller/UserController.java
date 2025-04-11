package apartments.controller;

import apartments.dto.request.user.ForgotPasswordRequest;
import apartments.dto.request.user.ResetPasswordRequest;
import apartments.dto.response.ForgotPasswordResponse;
import apartments.dto.response.ResetPasswordResponse;
import apartments.service.AuthenticationService;
import apartments.service.UserService;
import jakarta.validation.Valid;
import apartments.dto.request.user.UserCreationRequest;
import apartments.dto.request.user.UserUpdateRequest;
import apartments.dto.response.ApiResponse;
import apartments.dto.response.UserResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    AuthenticationService authenticationService;

    @PostMapping
    ApiResponse<UserResponse> createUser( @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.getUsers())
                .build();
    }

    @GetMapping("/myInfo")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyInfo())
                .build();
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.getUser(userId))
                .build();
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable("userId") String userId,
                                         @RequestBody @Valid UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<String> deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .data("User has been deleted")
                .build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ApiResponse.<ForgotPasswordResponse>builder()
                .data(userService.forgotPassword(request))
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ApiResponse.<ResetPasswordResponse>builder()
                .data(authenticationService.resetPassword(request.getToken(), request.getNewPassword()))
                .build();
    }

}