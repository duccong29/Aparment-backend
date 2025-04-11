package apartments.service;

import apartments.dao.LocationRepository;
import apartments.dto.request.apartment.LocationRequest;
import apartments.dto.response.DistrictResponse;
import apartments.dto.response.LocationResponse;
import apartments.dto.response.ProvinceResponse;
import apartments.dto.response.WardResponse;
import apartments.entity.Location;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.mapper.LocationMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_URL = "https://provinces.open-api.vn/api";

    LocationRepository locationRepository;
    LocationMapper locationMapper;

    public List<ProvinceResponse> getAllProvinces() {
        try {
            String url = API_URL + "/p/";
            ProvinceResponse[] provinces = restTemplate.getForObject(url, ProvinceResponse[].class);
            return provinces != null ? Arrays.asList(provinces) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch provinces", e);
            return Collections.emptyList();
        }
    }

    public List<DistrictResponse> getDistrictsByProvince(String provinceCode) {
        try {
            String url = String.format("%s/p/%s?depth=2", API_URL, provinceCode);
            ProvinceResponse province = restTemplate.getForObject(url, ProvinceResponse.class);
            return province != null && province.getDistricts() != null
                    ? province.getDistricts()
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch districts for province {}", provinceCode, e);
            return Collections.emptyList();
        }
    }

    public List<WardResponse> getWardsByDistrict(String districtCode) {
        try {
            String url = String.format("%s/d/%s?depth=2", API_URL, districtCode);
            DistrictResponse district = restTemplate.getForObject(url, DistrictResponse.class);
            return district != null && district.getWards() != null
                    ? district.getWards()
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to fetch wards for district {}", districtCode, e);
            return Collections.emptyList();
        }
    }

    public Location createLocation(LocationRequest request) {
        String provinceName = getProvinceName(request.getProvinceCode());
        String districtName = getDistrictName(request.getProvinceCode(), request.getDistrictCode());
        String wardName = getWardName(request.getDistrictCode(), request.getWardCode());

        Location location = locationMapper.toEntity(request);
        location.setProvinceName(provinceName);
        location.setDistrictName(districtName);
        location.setWardName(wardName);
        location.setStreet(request.getStreet());

        LocationResponse response = locationMapper.toResponse(location);
        location.setFullAddress(response.getFullAddress());

        return locationRepository.save(location);
    }

    public Location updateLocation(Location existingLocation, LocationRequest request) {
        existingLocation.setStreet(request.getStreet());
        boolean provinceChanged = !existingLocation.getProvinceCode().equals(request.getProvinceCode());
        boolean districtChanged = !existingLocation.getDistrictCode().equals(request.getDistrictCode());
        boolean wardChanged = !existingLocation.getWardCode().equals(request.getWardCode());

        if (provinceChanged || districtChanged || wardChanged) {
            String provinceName = provinceChanged ? getProvinceName(request.getProvinceCode())
                    : existingLocation.getProvinceName();
            String districtName = (provinceChanged || districtChanged)
                    ? getDistrictName(request.getProvinceCode(), request.getDistrictCode())
                    : existingLocation.getDistrictName();
            String wardName = (districtChanged || wardChanged)
                    ? getWardName(request.getDistrictCode(), request.getWardCode())
                    : existingLocation.getWardName();

            existingLocation.setProvinceCode(request.getProvinceCode());
            existingLocation.setDistrictCode(request.getDistrictCode());
            existingLocation.setWardCode(request.getWardCode());
            existingLocation.setProvinceName(provinceName);
            existingLocation.setDistrictName(districtName);
            existingLocation.setWardName(wardName);
        }

        LocationResponse response = locationMapper.toResponse(existingLocation);
        existingLocation.setFullAddress(response.getFullAddress());

        return locationRepository.save(existingLocation);
    }

    private String getProvinceName(String provinceCode) {
        try {
            String url = String.format("%s/p/%s", API_URL, provinceCode);
            ProvinceResponse province = restTemplate.getForObject(url, ProvinceResponse.class);
            return province != null ? province.getName() : "";
        } catch (Exception e) {
            log.error("Failed to fetch province name for code {}", provinceCode, e);
            return "";
        }
    }

    private String getDistrictName(String provinceCode, String districtCode) {
        try {
            String url = String.format("%s/p/%s?depth=2", API_URL, provinceCode);
            ProvinceResponse province = restTemplate.getForObject(url, ProvinceResponse.class);

            if (province == null || province.getDistricts() == null) {
                throw new AppException(ErrorCodes.INVALID_DISTRICT_CODE);
            }

            return province.getDistricts().stream()
                    .filter(district -> district.getCode().equals(districtCode))
                    .findFirst()
                    .map(DistrictResponse::getName)
                    .orElseThrow(() -> new AppException(ErrorCodes.INVALID_DISTRICT_CODE));
        } catch (Exception e) {
            log.error("Failed to fetch district name for code {}", districtCode, e);
            throw new AppException(ErrorCodes.INVALID_DISTRICT_CODE);
        }
    }

    private String getWardName(String districtCode, String wardCode) {
        try {
            String url = String.format("%s/d/%s?depth=2", API_URL, districtCode);
            DistrictResponse district = restTemplate.getForObject(url, DistrictResponse.class);

            if (district == null || district.getWards() == null) {
                throw new AppException(ErrorCodes.INVALID_WARD_CODE);
            }

            return district.getWards().stream()
                    .filter(ward -> ward.getCode().equals(wardCode))
                    .findFirst()
                    .map(WardResponse::getName)
                    .orElseThrow(() -> new AppException(ErrorCodes.INVALID_WARD_CODE));
        } catch (Exception e) {
            log.error("Failed to fetch ward name for code {}", wardCode, e);
            throw new AppException(ErrorCodes.INVALID_WARD_CODE);
        }
    }
}
