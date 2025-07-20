package vine.vine.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "vine.encryption")
@Data
public class EncryptionConfig {

    private String key = "DefaultVineKey123";
    private String algorithm = "AES";
    private String mode = "CBC";
    private String padding = "PKCS5Padding";

    // Optional: Different keys for different types of data
    private String passwordKey = "VinePasswordKey456";
    private String dataKey = "VineDataKey789";

    // Security settings
    private boolean requireEncryption = true;
    private int keyLength = 256;

    public String getTransformation() {
        return algorithm + "/" + mode + "/" + padding;
    }
}