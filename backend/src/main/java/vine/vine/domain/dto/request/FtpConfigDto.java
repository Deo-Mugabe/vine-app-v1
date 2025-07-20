package vine.vine.domain.dto.request;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FtpConfigDto {
    private String vineFtpUserName;
    private String vineFtpPassword;
    private String vinePrimaryFtpServerName;
    private String vineFtpDatFolderName;
    private String vineFtpFirewallOutPort;
    private String vineFtpMugshotFolderName;
    private boolean vineUseSftp;

}