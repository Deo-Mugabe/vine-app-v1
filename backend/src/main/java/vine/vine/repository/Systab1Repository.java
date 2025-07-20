package vine.vine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vine.vine.domain.Systab1;

import java.util.Optional;

@Repository
public interface Systab1Repository extends JpaRepository<Systab1, Integer> {
    Optional<Systab1> findFirstByCodeAgcyAndCodeKey(String codeAgcy, String codeKey);
}

