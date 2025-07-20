package vine.vine.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Systab1Request {

    @NotNull
    private String codeAgcy;

    @NotNull
    private String descriptn;

    @NotNull
    private String code_key;

    @NotNull
    private String sys_msg;
}
