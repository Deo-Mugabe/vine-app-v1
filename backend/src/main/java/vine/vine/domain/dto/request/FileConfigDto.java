package vine.vine.domain.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileConfigDto {
    private String vineChargesFileHeader;
    private String vinePrisonerFileHeader;
    private String vineJailIdNumber;
    private String vineNewMugShotDirectory;
    private String vineMugShotDirectory;
    private  String VineNewVineFilePath;
    private  String VineInterFile;
}