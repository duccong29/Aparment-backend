package apartments.controller;

import apartments.dto.response.*;
import apartments.service.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locations")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {
    LocationService locationService;

    @GetMapping("/provinces")
    public ApiResponse<List<ProvinceResponse>> getAllProvinces() {
        return ApiResponse.<List<ProvinceResponse>>builder()
                .data(locationService.getAllProvinces())
                .build();
    }

    @GetMapping("/districts")
    public ApiResponse<List<DistrictResponse>> getDistrictsByProvince(@RequestParam String provinceCode) {
        return ApiResponse.<List<DistrictResponse>>builder()
                .data(locationService.getDistrictsByProvince(provinceCode))
                .build();
    }

    @GetMapping("/wards")
    public ApiResponse<List<WardResponse>> getWardsByDistrict(@RequestParam String districtCode) {
        return ApiResponse.<List<WardResponse>>builder()
                .data(locationService.getWardsByDistrict(districtCode))
                .build();
    }
}
