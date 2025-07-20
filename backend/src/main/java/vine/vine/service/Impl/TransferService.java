package vine.vine.service.Impl;

import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
@RequiredArgsConstructor
public class TransferService {

    private final SysConfigService vineConfig;
    private final DecryptionService decryptionService;
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    public boolean ftpDataAndMugshotFiles() {

        String datDirectory  = vineConfig.getConfig().getVineNewVineFilePath();
         String datFileName = vineConfig.getConfig().getVineInterFile();
          String localDatFilePath = Paths.get(datDirectory, datFileName).toString();

        if (localDatFilePath == null || localDatFilePath.isBlank()) {
            logger.error("Invalid FTP file path: {}", localDatFilePath);
            return false;
        }

        File file = new File(localDatFilePath);
        if (!file.exists()) {
            logger.error("Target file not found: {}", localDatFilePath);
            return false;
        }

        String remoteDatDir = vineConfig.getConfig().getVineFtpDatFolderName().trim();
        boolean datUploaded = uploadFile(localDatFilePath, remoteDatDir);

        if (datUploaded) {
            logger.info("DAT file uploaded. Proceeding with mugshot transfer...");
            return uploadMugshots();
        } else {
            logger.error("DAT file upload failed. Skipping mugshot upload.");
            return false;
        }
    }

    public boolean uploadFile(String localPath, String remoteDir) {
        if (vineConfig.getConfig().isVineUseSftp()) {
            return uploadSftp(localPath, remoteDir);
        } else {
            logger.warn("FTP upload is not yet implemented");
            return false;
        }
    }

    private boolean uploadSftp(String localPath, String remoteDir) {
        Session session = null;
        ChannelSftp sftp = null;

        String username = vineConfig.getConfig().getVineFtpUserName().trim();
        String password = decryptionService.decrypt(vineConfig.getConfig().getVineFtpPassword().trim());
        String host = vineConfig.getConfig().getVinePrimaryFtpServerName().trim();
        int port = resolvePort(vineConfig.getConfig().getVineFtpFirewallOutPort(), 22);

        Path sourcePath = Paths.get(localPath);
        String fileName = sourcePath.getFileName().toString();
        String remotePath = remoteDir + fileName;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(5000);

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;

            try (FileInputStream fis = new FileInputStream(localPath)) {
                sftp.put(fis, remotePath);
                logger.info("Uploaded {} to {}", fileName, remotePath);
                return true;
            }

        } catch (Exception e) {
            logger.error("SFTP upload failed: {}", e.getMessage(), e);
            return false;

        } finally {
            if (sftp != null) sftp.disconnect();
            if (session != null) session.disconnect();
        }
    }

    public boolean uploadMugshots() {
        String mugshotDir = vineConfig.getConfig().getVineNewMugShotDirectory();
        String remoteDir = vineConfig.getConfig().getVineFtpMugshotFolderName();

        File dir = new File(mugshotDir);
        if (!dir.exists() || !dir.isDirectory()) {
            logger.error("Mugshot directory not found: {}", mugshotDir);
            return false;
        }

        File[] mugshots = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".jpg"));
        if (mugshots == null || mugshots.length == 0) {
            logger.warn("No mugshots found in: {}", mugshotDir);
            return true;
        }

        boolean allSuccess = true;
        for (File mugshot : mugshots) {
            boolean uploaded = uploadFile(mugshot.getAbsolutePath(), remoteDir);
            if (!uploaded) {
                logger.error("Failed to upload mugshot: {}", mugshot.getName());
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    private int resolvePort(String portStr, int defaultPort) {
        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            return defaultPort;
        }
    }
}
