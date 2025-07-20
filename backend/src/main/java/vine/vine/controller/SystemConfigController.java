package vine.vine.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vine.vine.domain.dto.request.FileConfigDto;
import vine.vine.domain.dto.request.FtpConfigDto;
import vine.vine.domain.dto.request.SystemConfigUpdateRequest;
import vine.vine.domain.dto.response.SystemConfigDto;
import vine.vine.service.Impl.SysConfigService;


@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
@Slf4j
public class SystemConfigController {

    private final SysConfigService sysConfigService;

    // FTP Configuration APIs
    @GetMapping("/ftp")
    public ResponseEntity<FtpConfigDto> getFtpConfig() {
        try {
            FtpConfigDto ftpConfig = sysConfigService.getFtpConfig();
            return ResponseEntity.ok(ftpConfig);
        } catch (Exception e) {
            log.error("Error retrieving FTP configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/ftp")
    public ResponseEntity<FtpConfigDto> updateFtpConfig(@RequestBody FtpConfigDto ftpConfigDto) {
        try {
            FtpConfigDto updatedConfig = sysConfigService.updateFtpConfig(ftpConfigDto);
            return ResponseEntity.ok(updatedConfig);
        } catch (Exception e) {
            log.error("Error updating FTP configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // File Configuration APIs
    @GetMapping("/file")
    public ResponseEntity<FileConfigDto> getFileConfig() {
        try {
            FileConfigDto fileConfig = sysConfigService.getFileConfig();
            return ResponseEntity.ok(fileConfig);
        } catch (Exception e) {
            log.error("Error retrieving File configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/file")
    public ResponseEntity<FileConfigDto> updateFileConfig(@RequestBody FileConfigDto fileConfigDto) {
        try {
            FileConfigDto updatedConfig = sysConfigService.updateFileConfig(fileConfigDto);
            return ResponseEntity.ok(updatedConfig);
        } catch (Exception e) {
            log.error("Error updating File configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Generic CRUD Operations for System Configuration
    @GetMapping
    public ResponseEntity<List<SystemConfigDto>> getAllConfigs() {
        try {
            List<SystemConfigDto> configs = sysConfigService.getAllConfigs();
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            log.error("Error retrieving all configurations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SystemConfigDto> getConfigById(@PathVariable Long id) {
        try {
            SystemConfigDto config = sysConfigService.getConfigById(id);
            if (config != null) {
                return ResponseEntity.ok(config);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving configuration with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/name/{sysName}")
    public ResponseEntity<SystemConfigDto> getConfigByName(@PathVariable String sysName) {
        try {
            SystemConfigDto config = sysConfigService.getConfigByName(sysName);
            if (config != null) {
                return ResponseEntity.ok(config);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving configuration with name: {}", sysName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<SystemConfigDto> createConfig(@RequestBody SystemConfigUpdateRequest request) {
        try {
            SystemConfigDto createdConfig = sysConfigService.createConfig(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdConfig);
        } catch (Exception e) {
            log.error("Error creating configuration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SystemConfigDto> updateConfig(@PathVariable Long id, @RequestBody SystemConfigUpdateRequest request) {
        try {
            SystemConfigDto updatedConfig = sysConfigService.updateConfig(id, request);
            if (updatedConfig != null) {
                return ResponseEntity.ok(updatedConfig);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating configuration with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        try {
            boolean deleted = sysConfigService.deleteConfig(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting configuration with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}