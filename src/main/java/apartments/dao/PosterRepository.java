package apartments.dao;

import apartments.dto.request.poster.PosterRequest;
import apartments.entity.Poster;
import apartments.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosterRepository extends JpaRepository<Poster, String> {
    boolean existsByUserIdAndStatus(String userId, String status);

    List<Poster> findByUserIdAndStatus(String userId, String status);

    boolean existsByUserAndStatus(User user, String status);
}
