package apartments.controller;

import apartments.service.ImageService;
import apartments.dto.response.ApiResponse;
import apartments.dto.response.ImageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageController {
    ImageService imageService;

    @PostMapping("/upload")
    public ApiResponse<ImageResponse> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("apartmentId") String apartmentId) {
        ImageResponse imageResponse = imageService.uploadImage(file, apartmentId);
        return ApiResponse.<ImageResponse>builder()
                .data(imageResponse)
                .build();
    }

//    @DeleteMapping("/delete/{publicId}")
//    public ResponseEntity<String> deleteImage(@PathVariable String publicId) {
//        imageService.deleteImageFromCloudinary(publicId);
//        return ResponseEntity.ok("Image deleted successfully from Cloudinary");
//    }
    // Xóa hình ảnh theo ID
    @DeleteMapping("/{publicId}")
    public ApiResponse<String> deleteImage(@PathVariable String publicId) {
        imageService.deleteImage(publicId);
        return ApiResponse.<String>builder()
                .data("Image deleted successfully")
                .build();
    }

    @GetMapping("/{imageId}")
    public ApiResponse<ImageResponse> getImageById(@PathVariable Long imageId) {
        ImageResponse imageResponse = imageService.getImageById(imageId);
        return ApiResponse.<ImageResponse>builder()
                .data(imageResponse)
                .build();
    }

    @GetMapping("/apartments/{apartmentId}")
    public ApiResponse<List<ImageResponse>> getImagesByApartmentId(@PathVariable String apartmentId) {
        List<ImageResponse> imageResponses = imageService.getImagesByApartmentId(apartmentId);
        return ApiResponse.<List<ImageResponse>>builder()
                .data(imageResponses)
                .build();
    }
}
