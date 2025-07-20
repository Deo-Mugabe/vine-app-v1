package vine.vine.service.Impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Arrays;

@Service
public class DecryptionService {

    private static final Logger logger = LoggerFactory.getLogger(DecryptionService.class);

    @Value("${vine.encryption.key:DefaultVineKey123}")
    private String encryptionKey;

    @Value("${vine.encryption.algorithm:AES}")
    private String algorithm;

    @Value("${vine.encryption.mode:CBC}")
    private String mode;

    @Value("${vine.encryption.padding:PKCS5Padding}")
    private String padding;

    private static final String CHARSET = StandardCharsets.UTF_8.name();

    /**
     * Main decrypt method - attempts multiple decryption strategies
     */
    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.trim().isEmpty()) {
            logger.warn("Empty or null encrypted string provided");
            return encrypted;
        }

        String trimmed = encrypted.trim();

        // Try AES decryption first (most common)
        try {
            return decryptAES(trimmed);
        } catch (Exception e) {
            logger.debug("AES decryption failed, trying other methods: {}", e.getMessage());
        }

        // Try simple Base64 decoding
        try {
            return decryptBase64(trimmed);
        } catch (Exception e) {
            logger.debug("Base64 decryption failed: {}", e.getMessage());
        }

        // Try legacy/custom decryption (if you have a specific format)
        try {
            return decryptLegacy(trimmed);
        } catch (Exception e) {
            logger.debug("Legacy decryption failed: {}", e.getMessage());
        }

        // If all else fails, return as-is (might be plain text)
        logger.warn("All decryption methods failed for value, returning as-is");
        return encrypted;
    }

    /**
     * AES decryption with CBC mode
     */
    private String decryptAES(String encrypted) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);

        // Generate key from password
        SecretKeySpec keySpec = generateKeyFromPassword(encryptionKey);

        // Extract IV (first 16 bytes) and encrypted data
        byte[] iv = Arrays.copyOfRange(encryptedBytes, 0, 16);
        byte[] cipherText = Arrays.copyOfRange(encryptedBytes, 16, encryptedBytes.length);

        Cipher cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(cipherText);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Simple Base64 decoding (for passwords that are just encoded, not encrypted)
     */
    private String decryptBase64(String encrypted) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Legacy/custom decryption method - implement your specific logic here
     */
    private String decryptLegacy(String encrypted) throws Exception {
        // Example: Simple Caesar cipher or XOR decryption
        // Replace this with your actual legacy decryption logic

        // Example XOR decryption
        return decryptXOR(encrypted, encryptionKey);
    }

    /**
     * XOR decryption example
     */
    private String decryptXOR(String encrypted, String key) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[encryptedBytes.length];
        for (int i = 0; i < encryptedBytes.length; i++) {
            result[i] = (byte) (encryptedBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * Generate AES key from password using SHA-256
     */
    private SecretKeySpec generateKeyFromPassword(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, algorithm);
    }

    /**
     * Encrypt method (for testing purposes)
     */
    public String encrypt(String plainText) {
        try {
            return encryptAES(plainText);
        } catch (Exception e) {
            logger.error("Encryption failed: {}", e.getMessage());
            return plainText;
        }
    }

    /**
     * AES encryption with CBC mode
     */
    private String encryptAES(String plainText) throws Exception {
        SecretKeySpec keySpec = generateKeyFromPassword(encryptionKey);

        Cipher cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] iv = cipher.getIV();
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Test method to verify encryption/decryption works
     */
    public boolean testEncryption() {
        String testText = "TestPassword123!";
        try {
            String encrypted = encrypt(testText);
            String decrypted = decrypt(encrypted);
            boolean success = testText.equals(decrypted);

            if (success) {
                logger.info("Encryption/decryption test passed");
            } else {
                logger.error("Encryption/decryption test failed. Original: {}, Decrypted: {}", testText, decrypted);
            }

            return success;
        } catch (Exception e) {
            logger.error("Encryption test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if a string appears to be encrypted (contains only base64 characters)
     */
    public boolean isEncrypted(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        // Check if it's a valid base64 string
        try {
            Base64.getDecoder().decode(value.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}