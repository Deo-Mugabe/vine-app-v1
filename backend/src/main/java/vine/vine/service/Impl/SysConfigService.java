package vine.vine.service.Impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vine.vine.domain.SystemConfigEntity;
import vine.vine.domain.VineSystemConfig;
import vine.vine.domain.dto.request.FileConfigDto;
import vine.vine.domain.dto.request.FtpConfigDto;
import vine.vine.domain.dto.request.SystemConfigUpdateRequest;
import vine.vine.domain.dto.response.SystemConfigDto;
import vine.vine.repository.SystemConfigRepository;

@Service
@RequiredArgsConstructor // This generates a constructor for ALL final fields.
@Slf4j
public class SysConfigService {


    private final SystemConfigRepository sysCfgRepository;
    private VineSystemConfig vineSystemConfig;
    private final DecryptionService decryptionService;

    // FTP Configuration Keys
    private static final String FTP_USERNAME_KEY = "gcvineftpusername";
    private static final String FTP_PASSWORD_KEY = "gcvineftppassword";
    private static final String FTP_PRIMARY_SERVER_KEY = "gcvineprimaryftpservername";
    private static final String FTP_DAT_FOLDER_KEY = "gcvineftpdatfoldername";
    private static final String FTP_FIREWALL_PORT_KEY = "gnvineftpfirewalloutport";
    private static final String FTP_MUGSHOT_FOLDER_KEY = "gcvineftpmugshotfoldername";
    private static final String FTP_USE_SFTP_KEY = "glvineusesftp";

    // File Configuration Keys
    private static final String CHARGES_FILE_HEADER_KEY = "gcvinechargesfileheader";
    private static final String PRISONER_FILE_HEADER_KEY = "gcvineprisonerfileheader";
    private static final String JAIL_ID_NUMBER_KEY = "gcvinejailidnumber";
    private static final String NEW_MUGSHOT_DIR_KEY = "gcvinenewmugshotdirectory";
    private static final String MUGSHOT_DIR_KEY = "gcvinemugshotdirectory";
    private static final String NEW_VINE_FILE_PATH_KEY = "gcvinenewvinefilepath";
    private static final String INTERFILE_NAME_KEY = "gcvineinterfile";

    @PostConstruct
    public void init() {
        log.info("Initializing VineSystemConfig via @PostConstruct.");
        loadSystemConfiguration();
    }

    private void loadSystemConfiguration() {
        Map<String, String> configMap;
        try {
            configMap = sysCfgRepository.findAll().stream()
                    .filter(Objects::nonNull)
                    .filter(e -> e.getSysName() != null && e.getDefaValue() != null)
                    .map(e -> Map.entry(e.getSysName().toLowerCase().trim(), e.getDefaValue()))
                    .filter(e -> e.getKey().startsWith("gcvine") || e.getKey().startsWith("gnvine") || e.getKey().startsWith("glvine"))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (a, b) -> {
                                log.warn("Duplicate configuration key found: {}. Keeping the first value.", a);
                                return a;
                            }
                    ));
        } catch (Exception e) {
            log.error("Failed to load system configurations from database during initialization. Application may not function correctly.", e);
            throw new IllegalStateException("Failed to load system configuration during startup.", e);
        }

        // Fetch required configs using lowercased raw keys
        String vineChargesFileHeader = getRequiredConfig(configMap, CHARGES_FILE_HEADER_KEY, "Vine Charges File Header");
        String vinePrisonerFileHeader = getRequiredConfig(configMap, PRISONER_FILE_HEADER_KEY, "Vine Prisoner File Header");
        String vineJailIdNumber = getRequiredConfig(configMap, JAIL_ID_NUMBER_KEY, "Vine Jail ID Number");
        String vineFtpUserName = getRequiredConfig(configMap, FTP_USERNAME_KEY, "Vine FTP User Name");
        String vineFtpPassword = getRequiredConfig(configMap, FTP_PASSWORD_KEY, "Vine FTP Password");
        String vinePrimaryFtpServerName = getRequiredConfig(configMap, FTP_PRIMARY_SERVER_KEY, "Vine Primary FTP Server Name");
        String vineNewMugShotDirectory = getRequiredConfig(configMap, NEW_MUGSHOT_DIR_KEY, "Vine New Mug Shot Directory");
        String vineMugShotDirectory = getRequiredConfig(configMap, MUGSHOT_DIR_KEY, "Vine Mug Shot Directory");
        String VineFtpDatFolderName = getRequiredConfig(configMap, FTP_DAT_FOLDER_KEY, "vine ftp data folder name");
        String VineFtpFirewallOutPort = getRequiredConfig(configMap, FTP_FIREWALL_PORT_KEY, "vine ftp firewall out port");
        String VineFtpMugshotFolderName = getRequiredConfig(configMap, FTP_MUGSHOT_FOLDER_KEY, "vine ftp mugshot folder name");
        boolean VineUseSftp = parseFlexibleBoolean(getRequiredConfig(configMap, FTP_USE_SFTP_KEY, "vine uses ftp"));
        String vineNewVineFilePath = getRequiredConfig(configMap, NEW_VINE_FILE_PATH_KEY, "Vine New VINE File Path");
        String vineInterFile = getRequiredConfig(configMap, INTERFILE_NAME_KEY, "VINE Interfile Name");



        this.vineSystemConfig = new VineSystemConfig(
                vineChargesFileHeader,
                vinePrisonerFileHeader,
                vineJailIdNumber,
                vineFtpUserName,
                vineFtpPassword,
                vinePrimaryFtpServerName,
                vineNewMugShotDirectory,
                vineMugShotDirectory,
                VineFtpDatFolderName,
                VineFtpFirewallOutPort,
                VineFtpMugshotFolderName,
                VineUseSftp,
                vineNewVineFilePath,
                vineInterFile
        );

        log.info("VineSystemConfig initialized successfully.");
    }

    private boolean parseFlexibleBoolean(String value) {
        if (value == null) return false;
        String v = value.trim().toLowerCase();
        return v.equals("true") || v.equals(".t.") || v.equals("1");
    }

    private String getRequiredConfig(Map<String, String> configMap, String key, String friendlyName) {
        String value = configMap.getOrDefault(key, "");
        if (value.isEmpty()) {
            log.error("Critical configuration missing or empty: '{}' ({}). Application cannot proceed.", key, friendlyName);
            throw new IllegalStateException("Missing critical configuration: " + friendlyName + " (" + key + ")");
        }
        return value;
    }

    public VineSystemConfig getConfig() {
        return vineSystemConfig;
    }

    // CRUD Operations for FTP Configuration
    public FtpConfigDto getFtpConfig() {
        return new FtpConfigDto(
                vineSystemConfig.getVineFtpUserName(),
                vineSystemConfig.getVineFtpPassword(),
                vineSystemConfig.getVinePrimaryFtpServerName(),
                vineSystemConfig.getVineFtpDatFolderName(),
                vineSystemConfig.getVineFtpFirewallOutPort(),
                vineSystemConfig.getVineFtpMugshotFolderName(),
                vineSystemConfig.isVineUseSftp()
        );
    }

    @Transactional
    public FtpConfigDto updateFtpConfig(FtpConfigDto ftpConfigDto) {
        try {
            updateConfigValue(FTP_USERNAME_KEY, ftpConfigDto.getVineFtpUserName());

            // ✅ Encrypt before saving
            String encryptedPassword = decryptionService.encrypt(ftpConfigDto.getVineFtpPassword());
            updateConfigValue(FTP_PASSWORD_KEY, encryptedPassword);

            updateConfigValue(FTP_PRIMARY_SERVER_KEY, ftpConfigDto.getVinePrimaryFtpServerName());
            updateConfigValue(FTP_DAT_FOLDER_KEY, ftpConfigDto.getVineFtpDatFolderName());
            updateConfigValue(FTP_FIREWALL_PORT_KEY, ftpConfigDto.getVineFtpFirewallOutPort());
            updateConfigValue(FTP_MUGSHOT_FOLDER_KEY, ftpConfigDto.getVineFtpMugshotFolderName());
            updateConfigValue(FTP_USE_SFTP_KEY, String.valueOf(ftpConfigDto.isVineUseSftp()));

            loadSystemConfiguration();
            log.info("FTP configuration updated successfully.");
            return getFtpConfig();
        } catch (Exception e) {
            log.error("Failed to update FTP configuration", e);
            throw new RuntimeException("Failed to update FTP configuration", e);
        }
    }

    // CRUD Operations for File Configuration
    public FileConfigDto getFileConfig() {
        return new FileConfigDto(
                vineSystemConfig.getVineChargesfileheader(),
                vineSystemConfig.getVinePrisonerfileheader(),
                vineSystemConfig.getVineJailidnumber(),
                vineSystemConfig.getVineNewMugShotDirectory(),
                vineSystemConfig.getVineMugShotDirectory(),
                vineSystemConfig.getVineNewVineFilePath(),
                vineSystemConfig.getVineInterFile()
        );
    }

    @Transactional
    public FileConfigDto updateFileConfig(FileConfigDto fileConfigDto) {
        try {
            updateConfigValue(CHARGES_FILE_HEADER_KEY, fileConfigDto.getVineChargesFileHeader());
            updateConfigValue(PRISONER_FILE_HEADER_KEY, fileConfigDto.getVinePrisonerFileHeader());
            updateConfigValue(JAIL_ID_NUMBER_KEY, fileConfigDto.getVineJailIdNumber());
            updateConfigValue(NEW_MUGSHOT_DIR_KEY, fileConfigDto.getVineNewMugShotDirectory());
            updateConfigValue(MUGSHOT_DIR_KEY, fileConfigDto.getVineMugShotDirectory());
            updateConfigValue(NEW_VINE_FILE_PATH_KEY, fileConfigDto.getVineNewVineFilePath());
            updateConfigValue(INTERFILE_NAME_KEY, fileConfigDto.getVineInterFile());

            // Reload configuration after update
            loadSystemConfiguration();
            log.info("File configuration updated successfully.");
            return getFileConfig();
        } catch (Exception e) {
            log.error("Failed to update File configuration", e);
            throw new RuntimeException("Failed to update File configuration", e);
        }
    }

    // Generic CRUD Operations
    public List<SystemConfigDto> getAllConfigs() {
        return sysCfgRepository.findAll().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public SystemConfigDto getConfigById(Long id) {
        return sysCfgRepository.findById(id)
                .map(this::entityToDto)
                .orElse(null);
    }

    public SystemConfigDto getConfigByName(String sysName) {
        return sysCfgRepository.findAll().stream()
                .filter(config -> config.getSysName().equalsIgnoreCase(sysName))
                .findFirst()
                .map(this::entityToDto)
                .orElse(null);
    }

    @Transactional
    public SystemConfigDto createConfig(SystemConfigUpdateRequest request) {
        SystemConfigEntity entity = new SystemConfigEntity();
        entity.setSysName(request.getSysName());
        entity.setDefaValue(request.getDefaValue());

        SystemConfigEntity saved = sysCfgRepository.save(entity);
        log.info("Created new configuration: {}", request.getSysName());

        // Reload configuration if it's a vine-related config
        if (isVineConfig(request.getSysName())) {
            loadSystemConfiguration();
        }

        return entityToDto(saved);
    }

    @Transactional
    public SystemConfigDto updateConfig(Long id, SystemConfigUpdateRequest request) {
        return sysCfgRepository.findById(id)
                .map(entity -> {
                    entity.setSysName(request.getSysName());
                    entity.setDefaValue(request.getDefaValue());
                    SystemConfigEntity updated = sysCfgRepository.save(entity);
                    log.info("Updated configuration: {} with ID: {}", request.getSysName(), id);

                    // Reload configuration if it's a vine-related config
                    if (isVineConfig(request.getSysName())) {
                        loadSystemConfiguration();
                    }

                    return entityToDto(updated);
                })
                .orElse(null);
    }

    @Transactional
    public boolean deleteConfig(Long id) {
        if (sysCfgRepository.existsById(id)) {
            sysCfgRepository.deleteById(id);
            log.info("Deleted configuration with ID: {}", id);

            // Reload configuration after deletion
            loadSystemConfiguration();
            return true;
        }
        return false;
    }

    private void updateConfigValue(String key, String value) {
        sysCfgRepository.findAll().stream()
                .filter(config -> config.getSysName().equalsIgnoreCase(key))
                .findFirst()
                .ifPresent(config -> {
                    config.setDefaValue(value);
                    sysCfgRepository.save(config);
                });
    }

    private boolean isVineConfig(String sysName) {
        String lowerName = sysName.toLowerCase();
        return lowerName.startsWith("gcvine") || lowerName.startsWith("gnvine") || lowerName.startsWith("glvine");
    }

    private SystemConfigDto entityToDto(SystemConfigEntity entity) {
        return new SystemConfigDto(
                entity.getId(),
                entity.getSysName(),
                entity.getDefaValue()
        );
    }
}