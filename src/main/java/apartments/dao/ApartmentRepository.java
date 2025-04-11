package apartments.dao;

import apartments.entity.Apartment;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, String>, JpaSpecificationExecutor<Apartment> {
    List<Apartment> findAllByUserId(String userId);
    Page<Apartment> findByApartmentTypeName(String apartmentType, Pageable pageable);
    Page<Apartment> findByStatus(String status, Pageable pageable);
    Page<Apartment> findByStatusAndApartmentTypeName(String status, String apartmentType, Pageable pageable);

    @Query("SELECT a.user.id FROM Apartment a WHERE a.id = :apartmentId")
    String findUserIdByApartmentId(@Param("apartmentId") String apartmentId);
}
