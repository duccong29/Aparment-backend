package apartments.service;

import apartments.dao.ApartmentTypeRepository;
import apartments.dto.response.ApartmentTypeResponse;
import apartments.dto.response.PageResponse;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.dto.request.apartment.ApartmentTypeRequest;
import apartments.entity.ApartmentType;
import apartments.mapper.ApartmentTypeMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApartmentTypeService {
    ApartmentTypeRepository apartmentTypeRepository;
    ApartmentTypeMapper apartmentTypeMapper;
    UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    public ApartmentTypeResponse createApartmentType(ApartmentTypeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        String userName = userService.getUserNameById(userId);

        ApartmentType apartmentType = apartmentTypeMapper.toApartmentType(request);
        apartmentType.setUserName(userName);
        apartmentType.setStatus("AVAILABLE");
        apartmentType = apartmentTypeRepository.save(apartmentType);
        return apartmentTypeMapper.toApartmentTypeResponse(apartmentType);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApartmentTypeResponse updateApartmentType(String apartmentTypeId, ApartmentTypeRequest request) {
        ApartmentType apartmentType = apartmentTypeRepository.findById(apartmentTypeId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_TYPE_NOT_FOUND));

        apartmentTypeMapper.updateApartmentType(request, apartmentType);
        apartmentType = apartmentTypeRepository.save(apartmentType);

        return apartmentTypeMapper.toApartmentTypeResponse(apartmentType);
    }

    public ApartmentTypeResponse getApartmentTypeById(String apartmentTypeId) {
        return apartmentTypeMapper.toApartmentTypeResponse(
                apartmentTypeRepository.findById(apartmentTypeId)
                        .orElseThrow(() -> new AppException(ErrorCodes.USER_NOT_EXISTED)));
    }

    public PageResponse<ApartmentTypeResponse> getAllApartmentsType(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ApartmentType> apartmentTypePage = apartmentTypeRepository.findAll(pageable);
        return PageResponse.<ApartmentTypeResponse>builder()
                .currentPage(apartmentTypePage.getNumber())
                .totalPages(apartmentTypePage.getTotalPages())
                .pageSize(apartmentTypePage.getSize())
                .totalElements(apartmentTypePage.getTotalElements())
                .data(apartmentTypePage.getContent().stream()
                        .map(apartmentTypeMapper::toApartmentTypeResponse)
                        .toList())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteApartmentTypeById(String apartmentId) {
        if (apartmentTypeRepository.existsById(apartmentId)) {
            apartmentTypeRepository.deleteById(apartmentId);
        } else {
            throw new AppException(ErrorCodes.APARTMENT_TYPE_NOT_FOUND);
        }
    }
}
