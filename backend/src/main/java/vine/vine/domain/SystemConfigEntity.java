package vine.vine.domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
@Table(name="sys_cfg")
public class SystemConfigEntity {

    @Id
    @Column(name = "sys_cfgid")
    private Long id;

    @Column(name = "sysname", nullable = false)
    private String sysName;

    @Column(name = "defavalue")
    private String defaValue;
}
