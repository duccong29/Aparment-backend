package apartments.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Poster {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    Instant createdAt;
    Instant approvalDate;

    String status;

    String adminNote;

    @ManyToOne(fetch = FetchType.LAZY)
    User approvedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.approvalDate = null;
    }

    @PreUpdate
    public void preUpdate() {
        this.approvalDate = Instant.now();
    }
}
