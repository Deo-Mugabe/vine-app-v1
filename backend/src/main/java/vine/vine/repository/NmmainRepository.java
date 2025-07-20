package vine.vine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vine.vine.domain.Nmmain;

@Repository
public interface NmmainRepository extends JpaRepository<Nmmain, Long> {
    Optional<Nmmain> findFirstByAliasIdAndNameType(String aliasId, String nameType);

}
