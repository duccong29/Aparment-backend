package apartments.controller;

import apartments.dto.request.apartment.ApartmentRequest;
import apartments.dto.response.ApartmentResponse;
import apartments.dto.response.PageResponse;
import apartments.entity.ApartmentDocument;
import apartments.service.ApartmentService;
import apartments.service.SearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import apartments.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/apartments")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApartmentController {
    ApartmentService apartmentService;
    SearchService searchService;

    @GetMapping("/filter")
    public ApiResponse<PageResponse<ApartmentDocument>> searchApartments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String apartmentTypeName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "4") int size) {
        PageResponse<ApartmentDocument> result = searchService.searchApartments(
                title, minPrice, maxPrice, apartmentTypeName, page, size);
        return ApiResponse.<PageResponse<ApartmentDocument>>builder()
                .data(result)
                .build();
    }

    @GetMapping("/suggest")
    public List<String> suggestTitles(@RequestParam String prefix) {
        return searchService.suggestTitles(prefix);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ApiResponse<ApartmentResponse> createApartment(@ModelAttribute ApartmentRequest request) {
        return ApiResponse.<ApartmentResponse>builder()
                .data(apartmentService.createApartment(request))
                .build();
    }

    @PutMapping(path = "/{apartmentId}", consumes = {"multipart/form-data"})
    public ApiResponse<ApartmentResponse> updateApartment(
            @PathVariable String apartmentId, @ModelAttribute ApartmentRequest request) {
        return ApiResponse.<ApartmentResponse>builder()
                .data(apartmentService.updateApartment(apartmentId, request))
                .build();
    }

    @GetMapping("/{apartmentId}")
    public ApiResponse<ApartmentResponse> getApartmentById(@PathVariable String apartmentId) {
        return ApiResponse.<ApartmentResponse>builder()
                .data(apartmentService.getApartmentById(apartmentId))
                .build();
    }

    @GetMapping("/my-posts")
    ApiResponse<List<ApartmentResponse>> myPosts() {
        return ApiResponse.<List<ApartmentResponse>>builder()
                .data(apartmentService.getMyPosts())
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<ApartmentResponse>> getAllApartments(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "4") int size,
            @RequestParam(value = "apartmentType", required = false) String apartmentType) {
        return ApiResponse.<PageResponse<ApartmentResponse>>builder()
                .data(apartmentService.getAllApartments(page, size, apartmentType))
                .build();
    }

    @GetMapping("/available")
    public ApiResponse<PageResponse<ApartmentResponse>> getAvailableApartments(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "4") int size,
            @RequestParam(value = "apartmentType", required = false) String apartmentType) {
        return ApiResponse.<PageResponse<ApartmentResponse>>builder()
                .data(apartmentService.getAvailableApartments(page, size, apartmentType))
                .build();
    }
    @DeleteMapping("/{apartmentId}")
    public ApiResponse<String> deleteApartmentById(@PathVariable String apartmentId) {
        apartmentService.deleteApartmentById(apartmentId);
        return ApiResponse.<String>builder()
                .data("Apartment has been deleted")
                .build();
    }
}
