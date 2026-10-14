package inventory.management.api.user.repository;


import inventory.management.api.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findAllByUserId(Long userId);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
