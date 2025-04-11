package apartments.dao;

import apartments.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByApartmentIdAndHash(String apartmentId, String hash);
    Optional<Image> findByPublicId(String publicId);
}
