package apartments.controller;

import apartments.dto.request.poster.AdminActionRequest;
import apartments.dto.request.poster.ApprovePosterRequest;
import apartments.dto.response.ApiResponse;
import apartments.dto.response.PosterResponse;
import apartments.service.PosterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/poster")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PosterController {
    PosterService posterService;

    @PostMapping
    public ApiResponse<PosterResponse> createPoster() {
        return ApiResponse.<PosterResponse>builder()
                .data(posterService.createPoster())
                .build();
    }

    @GetMapping
    public ApiResponse<List<PosterResponse>> getAllPosters() {
        return ApiResponse.<List<PosterResponse>>builder()
                .data(posterService.getAllPosters())
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<PosterResponse>> getPostersByStatus(@PathVariable String status) {
        return ApiResponse.<List<PosterResponse>>builder()
                .data(posterService.getPostersByStatus(status))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PosterResponse> getPosterById(@PathVariable String id) {
        return ApiResponse.<PosterResponse>builder()
                .data(posterService.getPosterById(id))
                .build();
    }

    @PostMapping("/approve/{posterId}")
    public ApiResponse<PosterResponse> approvePoster(
            @PathVariable String posterId,
            @RequestBody ApprovePosterRequest request) {
        return ApiResponse.<PosterResponse>builder()
                .data(posterService.approvePoster(posterId, request))
                .build();
    }

    @PutMapping("/manual-approve/{posterId}")
    public ApiResponse<PosterResponse> manualApproveUser(
            @PathVariable String posterId,
            @RequestBody AdminActionRequest request) {
        return ApiResponse.<PosterResponse>builder()
                .data(posterService.manualApproveUser(posterId, request))
                .build();
    }

    @PutMapping("/revoke/{posterId}")
    public ApiResponse<PosterResponse> revokePosterRole(
            @PathVariable String posterId,
            @RequestBody AdminActionRequest request) {
        return ApiResponse.<PosterResponse>builder()
                .data( posterService.revokePosterRole(posterId, request))
                .build();
    }
}
