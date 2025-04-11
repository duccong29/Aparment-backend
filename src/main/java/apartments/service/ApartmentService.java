package apartments.service;

import apartments.constant.PredefinedRole;
import apartments.constant.PredefinedStatus;
import apartments.dao.*;
import apartments.dto.request.apartment.ApartmentRequest;
import apartments.dto.response.ApartmentResponse;
import apartments.dto.response.PageResponse;
import apartments.entity.*;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.mapper.ApartmentMapper;
import apartments.dto.response.ImageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApartmentService {
    ApartmentRepository apartmentRepository;
    UserRepository userRepository;
    ApartmentTypeRepository apartmentTypeRepository;
    ImageRepository imageRepository;
    LocationService locationService;
    ImageService imageService;
    AuthenticationService authenticationService;
    ApartmentMapper apartmentMapper;
    KafkaTemplate<String, Object> kafkaTemplate;

    @PreAuthorize("hasAnyRole('ADMIN', 'POSTER')")
    @Transactional
    public ApartmentResponse createApartment(ApartmentRequest request) {

        String userId = authenticationService.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCodes.USERS_NOT_FOUND));

        ApartmentType apartmentType = apartmentTypeRepository.findById(request.getApartmentTypeId())
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_TYPE_NOT_FOUND));

        validateImages(request.getImages());

        Apartment apartment = apartmentMapper.toApartment(request);
        apartment.setUser(user);
        apartment.setApartmentType(apartmentType);
        apartment.setStatus(PredefinedStatus.APARTMENT_AVAILABLE);

        Location location = locationService.createLocation(request.getLocation());
        apartment.setLocation(location);

        Apartment savedApartment = apartmentRepository.save(apartment);

        List<Image> images = new ArrayList<>();
        for (MultipartFile imageFile : request.getImages()) {
            ImageResponse imageResponse = imageService.uploadImage(imageFile, savedApartment.getId());
            Image image = imageRepository.findById(imageResponse.getId())
                    .orElseThrow(() -> new AppException(ErrorCodes.IMAGE_NOT_FOUND));
            images.add(image);
        }

        savedApartment.setImages(images);
        apartmentRepository.save(savedApartment);

        ApartmentDocument apartmentDocument = apartmentMapper.toDocument(savedApartment);
        kafkaTemplate.send("apartments-topic", apartmentDocument);

        return apartmentMapper.toApartmentResponse(savedApartment);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'POSTER')")
    @Transactional
    public ApartmentResponse updateApartment(String apartmentId, ApartmentRequest request) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));

        if (!apartment.getUser().getId().equals(authenticationService.getCurrentUserId()) && !authenticationService.isAdmin()) {
            throw new AppException(ErrorCodes.UNAUTHORIZED);
        }

        validateImages(request.getImages());

        apartmentMapper.updateApartment(request, apartment);

        // Cập nhật loại căn hộ nếu có thay đổi
        if (!request.getApartmentTypeId().equals(apartment.getApartmentType().getId())) {
            ApartmentType newType = apartmentTypeRepository.findById(request.getApartmentTypeId())
                    .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_TYPE_NOT_FOUND));
            apartment.setApartmentType(newType);
        }

        // Cập nhật location nếu có thay đổi
        if (request.getLocation() != null && apartment.getLocation() != null) {
            Location updatedLocation = locationService.updateLocation(
                    apartment.getLocation(),
                    request.getLocation()
            );
            apartment.setLocation(updatedLocation);
        }

        handleImageUpdates(apartment, request.getImages());

        apartment.setStatus(PredefinedStatus.APARTMENT_AVAILABLE);
        Apartment updatedApartment = apartmentRepository.save(apartment);

        ApartmentDocument apartmentDocument = apartmentMapper.toDocument(updatedApartment);
        kafkaTemplate.send("apartments-topic", apartmentDocument);

        return apartmentMapper.toApartmentResponse(updatedApartment);
    }


    @Transactional(readOnly = true)
    public ApartmentResponse getApartmentById(String apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));
        return apartmentMapper.toApartmentResponse(apartment);
    }

    @Transactional(readOnly = true)
    public List<ApartmentResponse> getMyPosts() {
        String userId = authenticationService.getCurrentUserId();

        return apartmentRepository.findAllByUserId(userId)
                .stream()
                .map(apartmentMapper::toApartmentResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ApartmentResponse> getAvailableApartments(int page, int size, String apartmentType) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Apartment> apartmentPage;

        if (apartmentType == null || apartmentType.equalsIgnoreCase("all")) {
            apartmentPage = apartmentRepository.findByStatus(PredefinedStatus.APARTMENT_AVAILABLE, pageable);
        } else {
            apartmentPage = apartmentRepository.findByStatusAndApartmentTypeName(PredefinedStatus.APARTMENT_AVAILABLE, apartmentType, pageable);
        }

        return PageResponse.<ApartmentResponse>builder()
                .currentPage(apartmentPage.getNumber())
                .totalPages(apartmentPage.getTotalPages())
                .pageSize(apartmentPage.getSize())
                .totalElements(apartmentPage.getTotalElements())
                .data(apartmentPage.getContent().stream()
                        .map(apartmentMapper::toApartmentResponse)
                        .toList())
                .build();
    }



    @Transactional(readOnly = true)
    public PageResponse<ApartmentResponse> getAllApartments(int page, int size, String apartmentType) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Apartment> apartmentPage;
        if (apartmentType == null || apartmentType.equalsIgnoreCase("all")) {
            apartmentPage = apartmentRepository.findAll(pageable);
        } else {
            apartmentPage = apartmentRepository.findByApartmentTypeName(apartmentType, pageable);
        }

        return PageResponse.<ApartmentResponse>builder()
                .currentPage(apartmentPage.getNumber())
                .totalPages(apartmentPage.getTotalPages())
                .pageSize(apartmentPage.getSize())
                .totalElements(apartmentPage.getTotalElements())
                .data(apartmentPage.getContent().stream()
                        .map(apartmentMapper::toApartmentResponse)
                        .toList())
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'POSTER')")
    @Transactional
    public void deleteApartmentById(String apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));

        if (!apartment.getUser().getId().equals(authenticationService.getCurrentUserId()) && !authenticationService.isAdmin()) {
            throw new AppException(ErrorCodes.UNAUTHORIZED);
        }


        List<Image> images = apartment.getImages();
        if (images != null) {
            for (Image image : images) {
                imageService.deleteImage(image.getPublicId());
            }
        }
        apartmentRepository.delete(apartment);
        kafkaTemplate.send("apartments-delete-topic", apartmentId);
    }

    private void handleImageUpdates(Apartment apartment, List<MultipartFile> newImages) {
        Map<String, Image> existingImageMap = apartment.getImages().stream()
                .filter(img -> img.getHash() != null)
                .collect(Collectors.toMap(Image::getHash, Function.identity()));

        Set<String> newHashes = new HashSet<>();
        List<Image> updatedImages = new ArrayList<>();

        for (MultipartFile imageFile : newImages) {
            try (InputStream inputStream = imageFile.getInputStream()) {

                String hash = ImageService.calculateSHA256(inputStream);
                newHashes.add(hash);

                if (existingImageMap.containsKey(hash)) {
                    updatedImages.add(existingImageMap.get(hash));
                } else {
                    // Upload ảnh mới
                    ImageResponse imageResponse = imageService.uploadImage(imageFile, apartment.getId());
                    Image newImage = imageRepository.findById(imageResponse.getId())
                            .orElseThrow(() -> new AppException(ErrorCodes.IMAGE_NOT_FOUND));
                    updatedImages.add(newImage);
                }

            } catch (IOException ex) {
                throw new AppException(ErrorCodes.FILE_STORAGE_EXCEPTION);
            }
        }

        List<Image> imagesToDelete = apartment.getImages().stream()
                .filter(img -> !newHashes.contains(img.getHash()))
                .toList();

        apartment.getImages().removeAll(imagesToDelete);

        imagesToDelete.forEach(img -> {
            log.info("Deleting image with publicId: {}", img.getPublicId());
            imageService.deleteImage(img.getPublicId());
        });

        apartment.setImages(updatedImages);
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new AppException(ErrorCodes.IMAGES_REQUIRED);
        }

        if (images.size() > 4) {
            throw new AppException(ErrorCodes.TOO_MANY_IMAGES);
        }

        List<String> allowedContentTypes = Arrays.asList("image/jpeg", "image/png", "image/webp", "image/jpg");
        long maxSize = 5 * 1024 * 1024;
        for (MultipartFile image : images) {
            if (!allowedContentTypes.contains(image.getContentType())) {
                throw new AppException(ErrorCodes.INVALID_IMAGE_FORMAT);
            }
            if (image.getSize() > maxSize) {
                throw new AppException(ErrorCodes.FILE_TOO_LARGE);
            }
        }
    }

    public void updateApartmentStatusToBooked(String apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));

        if (PredefinedStatus.APARTMENT_AVAILABLE.equals(apartment.getStatus())) {
            apartment.setStatus(PredefinedStatus.APARTMENT_BOOKED);
            apartmentRepository.save(apartment);
        }
    }
}
