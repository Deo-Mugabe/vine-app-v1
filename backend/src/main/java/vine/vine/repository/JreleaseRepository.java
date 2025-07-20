package vine.vine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vine.vine.domain.Jrelease;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JreleaseRepository extends JpaRepository<Jrelease, Long> {
}
