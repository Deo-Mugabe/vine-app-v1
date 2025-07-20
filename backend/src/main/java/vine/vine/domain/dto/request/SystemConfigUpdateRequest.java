package vine.vine.domain.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigUpdateRequest {
    private String sysName;
    private String defaValue;
}