package vine.vine.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class VineSystemConfig {
    private final String vineChargesfileheader;
    private final String vinePrisonerfileheader;
    private final String vineJailidnumber;
    private final String vineFtpUserName;
    private final String vineFtpPassword;
    private final String vinePrimaryFtpServerName;
    private final String vineNewMugShotDirectory;
    private final String vineMugShotDirectory;
    private final String VineFtpDatFolderName;
    private final String VineFtpFirewallOutPort;
    private final String VineFtpMugshotFolderName;
    private final boolean VineUseSftp;
    private final String VineNewVineFilePath;
    private final String VineInterFile;
}
