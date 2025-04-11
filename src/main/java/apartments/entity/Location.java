package apartments.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String provinceCode;
    String provinceName;
    String districtCode;
    String districtName;
    String wardCode;
    String wardName;
    String street;
    String fullAddress;

    @OneToOne(mappedBy = "location")
    Apartment apartment;
}
