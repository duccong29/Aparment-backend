package apartments.controller;

import apartments.dto.request.apartment.ApartmentTypeRequest;
import apartments.dto.response.ApartmentTypeResponse;
import apartments.dto.response.PageResponse;
import apartments.service.ApartmentTypeService;
import apartments.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apartment-types")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApartmentTypeController {
    ApartmentTypeService apartmentTypeService;

    @PostMapping
    ApiResponse<ApartmentTypeResponse>  createApartmentType(@RequestBody ApartmentTypeRequest request) {
        return ApiResponse.<ApartmentTypeResponse>builder()
                .data(apartmentTypeService.createApartmentType(request))
                .build();
    }

    @PutMapping("/{apartmentTypeId}")
    ApiResponse<ApartmentTypeResponse> updateApartmentType(@PathVariable String apartmentTypeId, @RequestBody ApartmentTypeRequest request) {
        return ApiResponse.<ApartmentTypeResponse>builder()
                .data(apartmentTypeService.updateApartmentType(apartmentTypeId, request))
                .build();
    }

    @GetMapping("/{apartmentTypeId}")
    ApiResponse<ApartmentTypeResponse>  getApartmentTypeById(@PathVariable String apartmentTypeId) {
        return ApiResponse.<ApartmentTypeResponse>builder()
                .data(apartmentTypeService.getApartmentTypeById(apartmentTypeId))
                .build();
    }

    @GetMapping
    ApiResponse<PageResponse<ApartmentTypeResponse>>  getAllApartmentTypes(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "4") int size) {
        return ApiResponse.<PageResponse<ApartmentTypeResponse>>builder()
                .data(apartmentTypeService.getAllApartmentsType(page, size))
                .build();
    }

    @DeleteMapping("/{apartmentTypeId}")
    ApiResponse<String> deleteApartmentTypeById(@PathVariable String apartmentTypeId) {
        apartmentTypeService.deleteApartmentTypeById(apartmentTypeId);
        return ApiResponse.<String>builder()
                .data("Apartment type has been deleted")
                .build();
    }

}
