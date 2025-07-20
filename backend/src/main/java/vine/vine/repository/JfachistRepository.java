package vine.vine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vine.vine.domain.Jfachist;

import java.util.Optional;

@Repository
public interface JfachistRepository extends JpaRepository<Jfachist, Long> {
    Optional<Jfachist> findFirstByBookIdOrderByEventDateDesc(Long bookId);
}
