package com.example.mobiletemplate;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Manages encryption keys in Android KeyStore with biometric protection
 */
public class SecureKeyManager {
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    
    // User must authenticate within 30 seconds of key usage
    private static final int AUTH_VALIDITY_DURATION = 30;

    /**
     * Generate a master encryption key in KeyStore protected by biometric auth
     */
    public void generateMasterKey(String alias) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        );

        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256) // AES-256
            .setUserAuthenticationRequired(true)
            // Use 0 seconds - auth required per operation, allows creating Cipher before auth
            .setUserAuthenticationParameters(
                0,  // 0 = auth required for each use
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
            .build();

        keyGenerator.init(spec);
        keyGenerator.generateKey();
    }

    /**
     * Encrypt data with a KeyStore key (requires recent biometric auth)
     */
    public EncryptedData encryptData(String plaintext, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        
        SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] iv = cipher.getIV();
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        return new EncryptedData(
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        );
    }

    /**
     * Encrypt data with a pre-authenticated Cipher (for biometric flow)
     */
    public EncryptedData encryptDataWithCipher(String plaintext, Cipher cipher) throws Exception {
        byte[] iv = cipher.getIV();
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        return new EncryptedData(
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        );
    }

    /**
     * Get a Cipher for encryption (to use with BiometricPrompt)
     */
    public Cipher getEncryptCipher(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        
        SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        return cipher;
    }

    /**
     * Get a Cipher for decryption (to use with BiometricPrompt)
     */
    public Cipher getDecryptCipher(String alias, String ivString) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        
        SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
        byte[] iv = Base64.decode(ivString, Base64.NO_WRAP);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        
        return cipher;
    }

    /**
     * Decrypt data with a pre-authenticated Cipher (for biometric flow)
     */
    public String decryptDataWithCipher(String encryptedData, Cipher cipher) throws Exception {
        byte[] encrypted = Base64.decode(encryptedData, Base64.NO_WRAP);
        byte[] decryptedBytes = cipher.doFinal(encrypted);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Decrypt data with a KeyStore key (requires recent biometric auth)
     */
    public String decryptData(String encryptedData, String ivString, String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        
        SecretKey secretKey = (SecretKey) keyStore.getKey(alias, null);
        
        byte[] iv = Base64.decode(ivString, Base64.NO_WRAP);
        byte[] encrypted = Base64.decode(encryptedData, Base64.NO_WRAP);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        
        byte[] decryptedBytes = cipher.doFinal(encrypted);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Delete a key from KeyStore
     */
    public void deleteKey(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        keyStore.deleteEntry(alias);
    }

    /**
     * Check if a key exists in KeyStore
     */
    public boolean keyExists(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        return keyStore.containsAlias(alias);
    }

    /**
     * Data class for encrypted content
     */
    public static class EncryptedData {
        public final String ciphertext;
        public final String iv;

        public EncryptedData(String ciphertext, String iv) {
            this.ciphertext = ciphertext;
            this.iv = iv;
        }
    }
}
