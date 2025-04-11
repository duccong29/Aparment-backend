package apartments.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCodes {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 4 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    APARTMENT_TYPE_NOT_FOUND(1009,"Apartment type not found.", HttpStatus.NOT_FOUND),
    APARTMENT_NOT_FOUND(1010,"Apartment not found.", HttpStatus.NOT_FOUND),
    INVALID_FILE(1011,"Cannot store file outside current directory.", HttpStatus.BAD_REQUEST),
    FILE_STORAGE_EXCEPTION(1012,"Failed to store file.", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND(1013,"Images not found", HttpStatus.BAD_REQUEST),
    INVALID_IMAGES(1014, "Must have at least 1 image", HttpStatus.BAD_REQUEST),
    INVALID_CONFIRMATION_TOKEN(1015, "Invalid confirmation token.", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_CONFIRMED(1016, "You have not identified your email.",  HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1017, "Invalid email format.",  HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1018, "Email already exists.",  HttpStatus.BAD_REQUEST),
    USERS_NOT_FOUND(1010,"Users not found.", HttpStatus.NOT_FOUND),
    EMAIL_NOT_FOUND(1011,"Email not found.", HttpStatus.NOT_FOUND),
    EXPIRED_RESET_TOKEN(1012,"Expired reset token.", HttpStatus.NOT_FOUND),
    ENTITY_NOT_FOUND(1013, "Apartment not found.", HttpStatus.NOT_FOUND),
    USER_ALREADY_ACTIVATED(1014, "User is activated.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1015, "Invalid token.", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN_TYPE(1016, "invalid token type.", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_FOUND(1017,"Notification not found with id.", HttpStatus.NOT_FOUND),
    IO_EXCEPTION(1018, "IO error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_EXCEPTION(1019, "File delete failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PUBLIC_ID(1020, "INVALID_PUBLIC_ID", HttpStatus.BAD_REQUEST),
    INVALID_DISTRICT_CODE(1020, "The district code is not valid for the selected province", HttpStatus.BAD_REQUEST),
    INVALID_WARD_CODE(1021, "The ward/commune code is invalid for the selected district", HttpStatus.BAD_REQUEST),
    IMAGES_REQUIRED(1022, "At least one image is required", HttpStatus.BAD_REQUEST),
    IMAGE_FILE_EMPTY(1023, "File image cannot be empty", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1024,"Roles not found.", HttpStatus.NOT_FOUND),
    USER_ALREADY_POSTER(1025,"User already has a poster role.", HttpStatus.BAD_REQUEST),
    POSTER_REQUEST_PENDING(1026,"User already has pending request.", HttpStatus.BAD_REQUEST),
    POSTER_NOT_FOUND(1027,"Poster not found.", HttpStatus.NOT_FOUND),
    INVALID_POSTER_STATUS(1028,"Invalid Poster status invalid.", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(1029,"Invalid status invalid.", HttpStatus.BAD_REQUEST),
    TOO_MANY_IMAGES(1030,"Cannot exceed 4 images.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FORMAT(1031,"Invalid photo format.", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1031," Maximum file size: 5MB.", HttpStatus.BAD_REQUEST),
    USER_NOT_POSTER(1032," User does not have POSTER role.", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1033,"Users admin not found.", HttpStatus.NOT_FOUND),
    CHECKIN_DATE_INVALID(1034, "Check-in date is invalid.", HttpStatus.BAD_REQUEST),
    CHECKOUT_DATE_INVALID(1035, "Check-out date is invalid.", HttpStatus.BAD_REQUEST),
    GUESTS_INVALID(1036, "Number of guests must be greater than 0.", HttpStatus.BAD_REQUEST),
    APARTMENT_NOT_AVAILABLE(1036, "The apartment is not in an available state", HttpStatus.BAD_REQUEST),


    ;

    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}
