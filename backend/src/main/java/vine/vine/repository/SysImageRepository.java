package vine.vine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vine.vine.domain.SysImageEntity;

@Repository
public interface  SysImageRepository extends JpaRepository<SysImageEntity, Long>{
    List<SysImageEntity> findBySystemKeyAndSystemIdOrderByAddTimeDesc(String systemKey, Long systemId);
}
