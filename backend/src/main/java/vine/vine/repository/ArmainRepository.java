package vine.vine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vine.vine.domain.Armain;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArmainRepository extends JpaRepository<Armain, Long> {

    Optional<Armain> findFirstByBookIdOrderByArmainidAsc(Long bookId);
}
