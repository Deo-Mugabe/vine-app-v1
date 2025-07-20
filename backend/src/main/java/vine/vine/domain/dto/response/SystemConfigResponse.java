package vine.vine.domain.dto.response;

import lombok.Builder;
import lombok.Data;
import vine.vine.domain.SystemConfigEntity;

@Data
@Builder
public class SystemConfigResponse {
    private Long id;
    private String sysName;
    private String defaultValue;


    public static SystemConfigResponse from(SystemConfigEntity entity){
        if (entity == null) {
            return null; // Or throw an IllegalArgumentException, depending on desired behavior
        }
        return SystemConfigResponse.builder()
                .id(entity.getId())
                .sysName(entity.getSysName())
                .defaultValue(entity.getDefaValue())
                .build();
    }
    
}
