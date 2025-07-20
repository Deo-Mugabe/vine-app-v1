package vine.vine.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@Table(name="systab1")
@RequiredArgsConstructor
public class Systab1 {
    @Id
    @Column(name="systab1id")
    private Integer systab1id;

    @Column(name = "codeAgcy")
    private String codeAgcy;


    @Column(name = "code_key")
    private String codeKey;

    @Column(name = "sys_msg")
    private String sys_msg;

}
