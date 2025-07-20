package vine.vine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vine.vine.domain.SystemConfigEntity;

@Repository
public interface  SystemConfigRepository extends JpaRepository<SystemConfigEntity, Long>{
    
}
