package apartments.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class ApartmentType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String status;
    String userName;
    Instant createdDate;
    Instant modifiedDate;
    @OneToMany(mappedBy = "apartmentType", cascade = CascadeType.ALL)
    List<Apartment> apartments;

    @PrePersist
    public void prePersist() {
        this.createdDate = Instant.now();
        this.modifiedDate = null;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = Instant.now();
    }

}
