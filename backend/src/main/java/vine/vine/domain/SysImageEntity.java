package vine.vine.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Table(name="sys_img")
@RequiredArgsConstructor
public class SysImageEntity {
    @Id
    @Column(name="sys_imgid")
    private Long sysImgId;

    @Column(name="sysid")
    private Long systemId;

    @Column(name="syskey")
    private String systemKey;

    @Column(name="ext1")
    private Integer ext1;

    @Column(name="ext2")
    private Integer ext2;

    @Column(name="addtime")
    private LocalDateTime addTime;
    
}
