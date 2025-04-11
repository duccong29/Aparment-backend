package apartments.dto.request.apartment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApartmentRequest {
    @NotBlank(message = "Title is required")
    String title;
    @NotBlank(message = "Description is required")
    String description;
    @NotNull(message = "Price is required")
    Double price;
    @NotNull(message = "Area is required")
    Double area;
//    @NotBlank(message = "Status is required")
//    String status;

    @NotNull(message = "ApartmentType is required")
    String apartmentTypeId;

    @Size(min = 1, message = "INVALID_IMAGES")
    List<MultipartFile> images;

    LocationRequest location;

//    String userId;

}
