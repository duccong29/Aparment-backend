package apartments.service;

import apartments.dao.ApartmentRepository;
import apartments.dao.ImageRepository;
import apartments.entity.Apartment;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import apartments.dto.response.ImageResponse;
import apartments.entity.Image;
import apartments.exception.AppException;
import apartments.exception.ErrorCodes;
import apartments.mapper.ImageMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ImageService {
    ApartmentRepository apartmentRepository;
    ImageRepository imageRepository;
    ImageMapper imageMapper;
    Cloudinary cloudinary;


    public ImageResponse uploadImage(MultipartFile file, String apartmentId) {
        try (InputStream inputStream = file.getInputStream()) {

            String hash = calculateSHA256(inputStream);

            Optional<Image> existingImage = imageRepository.findByApartmentIdAndHash(apartmentId, hash);
            if (existingImage.isPresent()) {
                return imageMapper.toImageResponse(existingImage.get());
            }

            // Cloudinary vẫn yêu cầu byte[] nên đọc lại stream
            byte[] bytes = file.getBytes(); // chỉ dùng nếu cần upload
            Map<?, ?> uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.emptyMap());
            String url = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString();

            Apartment apartment = apartmentRepository.findById(apartmentId)
                    .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));

            Image image = new Image();
            image.setUrl(url);
            image.setPublicId(publicId);
            image.setHash(hash);
            image.setApartment(apartment);

            Image savedImage = imageRepository.save(image);
            return imageMapper.toImageResponse(savedImage);
        } catch (IOException ex) {
            throw new AppException(ErrorCodes.FILE_STORAGE_EXCEPTION);
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.error("Invalid publicId: {}", publicId);
            throw new AppException(ErrorCodes.INVALID_PUBLIC_ID);
        }

        Image image = imageRepository.findByPublicId(publicId)
                .orElseThrow(() -> {
                    log.error("Image not found with publicId: {}", publicId);
                    return new AppException(ErrorCodes.IMAGE_NOT_FOUND);
                });

        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Cloudinary deletion result for publicId {}: {}", publicId, result);

            imageRepository.delete(image);
            log.info("Deleted image from database: {}", image.getId());
        } catch (IOException ex) {
            log.error("Error deleting image from Cloudinary: {}", publicId, ex);
            throw new AppException(ErrorCodes.FILE_DELETE_EXCEPTION);
        }
    }

    public ImageResponse getImageById(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new AppException(ErrorCodes.IMAGE_NOT_FOUND));
        return imageMapper.toImageResponse(image);
    }

    public List<ImageResponse> getImagesByApartmentId(String apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCodes.APARTMENT_NOT_FOUND));
        return apartment.getImages().stream()
                .map(imageMapper::toImageResponse)
                .collect(Collectors.toList());
    }

    public static String calculateSHA256(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Error calculating SHA-256", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}