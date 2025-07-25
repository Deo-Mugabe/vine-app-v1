package vine.vine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vine.vine.domain.SchedulerConfigEntity;
import java.util.Optional;

@Repository
public interface SchedulerConfigRepository extends JpaRepository<SchedulerConfigEntity, String> {
    Optional<SchedulerConfigEntity> findByConfigName(String configName);
}