package com.example.mobiletemplate;

import android.util.Log;
import javax.crypto.Cipher;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import org.json.JSONArray;

@CapacitorPlugin(name = "SSHClient")
public class SSHJPlugin extends Plugin {

    private final Map<String, SSHClient> activeSessions = new ConcurrentHashMap<>();
    private SecureKeyManager secureKeyManager;
    private static final String PREFS_NAME = "ssh_keys_metadata";
    private static final String KEYS_KEY = "ssh_keys";

    @Override
    public void load() {
        super.load();
        secureKeyManager = new SecureKeyManager();
    }

    @PluginMethod
    public void connect(PluginCall call) {
        String hostname = call.getString("hostname");
        Integer port = call.getInt("port", 22);
        String username = call.getString("username");
        String authMethod = call.getString("authMethod");
        String password = call.getString("password");
        String privateKey = call.getString("privateKey");
        String privateKeyPath = call.getString("privateKeyPath");
        String passphrase = call.getString("passphrase");

        if (hostname == null || username == null || authMethod == null) {
            call.reject("Missing required parameters");
            return;
        }

        new Thread(() -> {
            try {
                // Create custom config to avoid X25519 issues on Android
                net.schmizz.sshj.DefaultConfig config = new net.schmizz.sshj.DefaultConfig();
                
                // Register BouncyCastle provider
                java.security.Security.removeProvider("BC");
                java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                
                SSHClient ssh = new SSHClient(config);
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                
                // Set timeout
                ssh.setTimeout(10000);
                ssh.setConnectTimeout(10000);
                
                ssh.connect(hostname, port);

                if ("password".equals(authMethod)) {
                    if (password == null || password.isEmpty()) {
                        ssh.disconnect();
                        call.reject("Password required for password auth");
                        return;
                    }
                    ssh.authPassword(username, password);
                } else if ("privateKey".equals(authMethod)) {
                    // Check if we have privateKey content or need to read from path
                    String keyContent = privateKey;
                    
                    if ((keyContent == null || keyContent.isEmpty()) && privateKeyPath != null && !privateKeyPath.isEmpty()) {
                        // Read key from file path
                        try {
                            java.io.File keyFile = new java.io.File(privateKeyPath);
                            if (!keyFile.exists()) {
                                ssh.disconnect();
                                call.reject("Private key file not found: " + privateKeyPath);
                                return;
                            }
                            
                            java.io.FileReader reader = new java.io.FileReader(keyFile);
                            java.io.BufferedReader bufferedReader = new java.io.BufferedReader(reader);
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            bufferedReader.close();
                            keyContent = sb.toString();
                        } catch (Exception readError) {
                            ssh.disconnect();
                            call.reject("Failed to read private key file: " + readError.getMessage());
                            return;
                        }
                    }
                    
                    if (keyContent == null || keyContent.isEmpty()) {
                        ssh.disconnect();
                        call.reject("Private key required for privateKey auth");
                        return;
                    }
                    
                    java.io.File tempKeyFile = null;
                    try {
                        // Write private key to temporary file
                        tempKeyFile = java.io.File.createTempFile("ssh_key_", ".pem");
                        tempKeyFile.deleteOnExit();
                        
                        java.io.FileWriter writer = new java.io.FileWriter(tempKeyFile);
                        writer.write(keyContent);
                        writer.close();
                        
                        // Make file readable only by owner
                        tempKeyFile.setReadable(false, false);
                        tempKeyFile.setReadable(true, true);
                        
                        // Load key from temporary file
                        KeyProvider keyProvider;
                        if (passphrase != null && !passphrase.isEmpty()) {
                            keyProvider = ssh.loadKeys(tempKeyFile.getAbsolutePath(), passphrase.toCharArray());
                        } else {
                            keyProvider = ssh.loadKeys(tempKeyFile.getAbsolutePath());
                        }
                        
                        ssh.authPublickey(username, keyProvider);
                    } catch (Exception keyError) {
                        ssh.disconnect();
                        call.reject("Failed to load private key: " + keyError.getMessage() + 
                                   (keyError.getCause() != null ? " - " + keyError.getCause().getMessage() : ""));
                        return;
                    } finally {
                        // Clean up temporary file
                        if (tempKeyFile != null && tempKeyFile.exists()) {
                            tempKeyFile.delete();
                        }
                    }
                } else {
                    ssh.disconnect();
                    call.reject("Unsupported auth method: " + authMethod);
                    return;
                }

                String sessionId = java.util.UUID.randomUUID().toString();
                activeSessions.put(sessionId, ssh);

                JSObject ret = new JSObject();
                ret.put("success", true);
                ret.put("sessionId", sessionId);
                ret.put("message", "Connected successfully");
                call.resolve(ret);

            } catch (Exception e) {
                String detailedError = e.getMessage();
                if (e.getCause() != null) {
                    detailedError += " - Cause: " + e.getCause().getMessage();
                }
                call.reject("Connection failed: " + detailedError);
            }
        }).start();
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        String sessionId = call.getString("sessionId");
        if (sessionId == null) {
            call.reject("Session ID required");
            return;
        }

        SSHClient ssh = activeSessions.remove(sessionId);
        if (ssh != null) {
            try {
                ssh.disconnect();
            } catch (IOException e) {
                // Ignore errors on disconnect
            }
        }

        JSObject ret = new JSObject();
        ret.put("success", true);
        call.resolve(ret);
    }

    @PluginMethod
    public void executeCommand(PluginCall call) {
        String sessionId = call.getString("sessionId");
        String command = call.getString("command");

        if (sessionId == null || command == null) {
            call.reject("Session ID and command required");
            return;
        }

        SSHClient ssh = activeSessions.get(sessionId);
        if (ssh == null || !ssh.isConnected()) {
            call.reject("Not connected");
            return;
        }

        new Thread(() -> {
            Session session = null;
            try {
                session = ssh.startSession();
                Session.Command cmd = session.exec(command);
                String output = readInputStream(cmd.getInputStream());
                String error = readInputStream(cmd.getErrorStream());
                cmd.join(5, TimeUnit.SECONDS);
                
                // Combine output and error or handle them as needed. 
                // For simplicity, we'll return output. If empty, return error.
                String resultOutput = output;
                if (error != null && !error.isEmpty()) {
                    resultOutput += "\nSTDERR:\n" + error;
                }

                JSObject ret = new JSObject();
                ret.put("success", true);
                ret.put("output", resultOutput);
                call.resolve(ret);

            } catch (Exception e) {
                call.reject("Command execution failed: " + e.getMessage());
            } finally {
                try {
                    if (session != null) {
                        session.close();
                    }
                } catch (IOException e) {
                    // Ignore
                }
            }
        }).start();
    }

    @PluginMethod
    public void isConnected(PluginCall call) {
        String sessionId = call.getString("sessionId");
        if (sessionId == null) {
            call.reject("Session ID required");
            return;
        }

        SSHClient ssh = activeSessions.get(sessionId);
        boolean connected = ssh != null && ssh.isConnected();

        JSObject ret = new JSObject();
        ret.put("connected", connected);
        call.resolve(ret);
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        java.util.Scanner s = new java.util.Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @PluginMethod
    public void generateKeyPair(PluginCall call) {
        String keyType = call.getString("keyType", "rsa");
        Integer keySize = call.getInt("keySize", 2048);
        String comment = call.getString("comment", "");

        new Thread(() -> {
            try {
                java.security.KeyPairGenerator generator;
                
                if ("rsa".equalsIgnoreCase(keyType)) {
                    generator = java.security.KeyPairGenerator.getInstance("RSA");
                    generator.initialize(keySize);
                } else if ("ed25519".equalsIgnoreCase(keyType)) {
                    // EdDSA support
                    generator = java.security.KeyPairGenerator.getInstance("Ed25519");
                } else {
                    call.reject("Unsupported key type: " + keyType + ". Supported types: rsa, ed25519");
                    return;
                }

                java.security.KeyPair keyPair = generator.generateKeyPair();
                
                // Convert to OpenSSH format
                String privateKeyPem = convertPrivateKeyToPEM(keyPair.getPrivate(), keyType);
                String publicKeyOpenSSH = convertPublicKeyToOpenSSH(keyPair.getPublic(), keyType, comment);
                
                JSObject ret = new JSObject();
                ret.put("success", true);
                ret.put("privateKey", privateKeyPem);
                ret.put("publicKey", publicKeyOpenSSH);
                ret.put("keyType", keyType);
                ret.put("keySize", keySize);
                call.resolve(ret);
                
            } catch (Exception e) {
                call.reject("Key generation failed: " + e.getMessage());
            }
        }).start();
    }

    @PluginMethod
    public void generateSecureKeyPair(PluginCall call) {
        String keyType = call.getString("keyType", "rsa");
        Integer keySize = call.getInt("keySize", 2048);
        String label = call.getString("label", "SSH Key");

        // Check if biometric is available
        if (!BiometricHelper.canAuthenticate(getContext())) {
            call.reject("Biometric authentication not available. Please set up fingerprint or face unlock in your device settings.");
            return;
        }

        new Thread(() -> {
            try {
                android.util.Log.d("SSHJPlugin", "Starting secure key generation for: " + label);
                
                // 1. Generate SSH keypair
                java.security.KeyPairGenerator generator;
                if ("rsa".equalsIgnoreCase(keyType)) {
                    generator = java.security.KeyPairGenerator.getInstance("RSA");
                    generator.initialize(keySize);
                } else if ("ed25519".equalsIgnoreCase(keyType)) {
                    generator = java.security.KeyPairGenerator.getInstance("Ed25519");
                } else {
                    call.reject("Unsupported key type");
                    return;
                }
                java.security.KeyPair keyPair = generator.generateKeyPair();
                android.util.Log.d("SSHJPlugin", "SSH keypair generated");
                
                // 2. Convert keys to strings
                String privateKeyPem = convertPrivateKeyToPEM(keyPair.getPrivate(), keyType);
                String publicKeyOpenSSH = convertPublicKeyToOpenSSH(keyPair.getPublic(), keyType, label);
                
                // 3. Generate unique ID and alias
                String keyId = java.util.UUID.randomUUID().toString();
                String alias = "ssh_master_" + keyId;
                
                // 4. Create master key in KeyStore (biometric-protected)
                android.util.Log.d("SSHJPlugin", "Creating KeyStore master key: " + alias);
                secureKeyManager.generateMasterKey(alias);
                
                // 5. Get encryption cipher (will need biometric auth to use)
                android.util.Log.d("SSHJPlugin", "Getting encryption cipher");
                javax.crypto.Cipher cipher = secureKeyManager.getEncryptCipher(alias);
                
                android.util.Log.d("SSHJPlugin", "Showing biometric prompt");
                
                // 6. Show biometric prompt BEFORE encrypting
                getActivity().runOnUiThread(() -> {
                    android.util.Log.d("SSHJPlugin", "BiometricHelper.authenticate called");
                    BiometricHelper.authenticate(
                        getActivity(),
                        cipher,
                        "Secure Your SSH Key",
                        "Authenticate to encrypt your private key",
                        "Your private key will be protected by biometric authentication",
                        new BiometricHelper.AuthenticationCallback() {
                            @Override
                            public void onSuccess(javax.crypto.Cipher authenticatedCipher) {
                                android.util.Log.d("SSHJPlugin", "Biometric authentication succeeded");
                                // Biometric success - proceed with encryption
                                new Thread(() -> {
                                    try {
                                        // 7. Encrypt private key with authenticated cipher
                                        SecureKeyManager.EncryptedData encrypted = 
                                            secureKeyManager.encryptDataWithCipher(privateKeyPem, authenticatedCipher);
                                        
                                        // 8. Store metadata in SharedPreferences
                                        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                                        JSONObject allKeys = new JSONObject(prefs.getString(KEYS_KEY, "{}"));
                                        
                                        JSONObject keyMeta = new JSONObject();
                                        keyMeta.put("alias", alias);
                                        keyMeta.put("publicKey", publicKeyOpenSSH);
                                        keyMeta.put("encryptedPrivateKey", encrypted.ciphertext);
                                        keyMeta.put("iv", encrypted.iv);
                                        keyMeta.put("label", label);
                                        keyMeta.put("keyType", keyType);
                                        keyMeta.put("keySize", keySize);
                                        keyMeta.put("createdAt", new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new java.util.Date()));
                                        
                                        allKeys.put(keyId, keyMeta);
                                        prefs.edit().putString(KEYS_KEY, allKeys.toString()).apply();
                                        
                                        android.util.Log.d("SSHJPlugin", "Key saved successfully: " + keyId);
                                        
                                        // 9. Return result (NO private key!)
                                        JSObject ret = new JSObject();
                                        ret.put("success", true);
                                        ret.put("keyId", keyId);
                                        ret.put("publicKey", publicKeyOpenSSH);
                                        ret.put("alias", alias);
                                        ret.put("label", label);
                                        call.resolve(ret);
                                        
                                    } catch (Exception e) {
                                        android.util.Log.e("SSHJPlugin", "Encryption failed", e);
                                        call.reject("Failed to encrypt key: " + e.getMessage());
                                    }
                                }).start();
                            }

                            @Override
                            public void onError(String message) {
                                android.util.Log.e("SSHJPlugin", "Biometric error: " + message);
                                // Clean up the KeyStore key on auth failure
                                try {
                                    secureKeyManager.deleteKey(alias);
                                } catch (Exception ignored) {}
                                call.reject("Biometric authentication failed: " + message);
                            }

                            @Override
                            public void onCancel() {
                                android.util.Log.d("SSHJPlugin", "Biometric canceled");
                                // Clean up the KeyStore key on cancel
                                try {
                                    secureKeyManager.deleteKey(alias);
                                } catch (Exception ignored) {}
                                call.reject("Key generation canceled");
                            }
                        }
                    );
                });
                
            } catch (Exception e) {
                android.util.Log.e("SSHJPlugin", "Secure key generation failed", e);
                call.reject("Secure key generation failed: " + e.getMessage() + " - " + e.getClass().getSimpleName());
            }
        }).start();
    }

    @PluginMethod
    public void listStoredKeys(PluginCall call) {
        try {
            android.util.Log.d("SSHJPlugin", "listStoredKeys called");
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String keysJson = prefs.getString(KEYS_KEY, "{}");
            android.util.Log.d("SSHJPlugin", "Stored keys JSON: " + keysJson);
            
            JSONObject allKeys = new JSONObject(keysJson);
            
            com.getcapacitor.JSArray keysArray = new com.getcapacitor.JSArray();
            java.util.Iterator<String> keys = allKeys.keys();
            while (keys.hasNext()) {
                String keyId = keys.next();
                JSONObject keyMeta = allKeys.getJSONObject(keyId);
                
                JSObject keyInfo = new JSObject();
                keyInfo.put("keyId", keyId);
                keyInfo.put("label", keyMeta.getString("label"));
                keyInfo.put("publicKey", keyMeta.getString("publicKey"));
                keyInfo.put("keyType", keyMeta.getString("keyType"));
                keyInfo.put("createdAt", keyMeta.getString("createdAt"));
                
                keysArray.put(keyInfo);
            }
            
            android.util.Log.d("SSHJPlugin", "Returning " + keysArray.length() + " keys");
            
            JSObject ret = new JSObject();
            ret.put("keys", keysArray);
            call.resolve(ret);
            
        } catch (Exception e) {
            android.util.Log.e("SSHJPlugin", "Failed to list keys", e);
            call.reject("Failed to list keys: " + e.getMessage());
        }
    }

    @PluginMethod
    public void deleteSecureKey(PluginCall call) {
        String keyId = call.getString("keyId");
        if (keyId == null) {
            call.reject("keyId required");
            return;
        }

        try {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            JSONObject allKeys = new JSONObject(prefs.getString(KEYS_KEY, "{}"));
            
            if (!allKeys.has(keyId)) {
                call.reject("Key not found");
                return;
            }
            
            JSONObject keyMeta = allKeys.getJSONObject(keyId);
            String alias = keyMeta.getString("alias");
            
            // Delete from KeyStore
            secureKeyManager.deleteKey(alias);
            
            // Delete from SharedPreferences
            allKeys.remove(keyId);
            prefs.edit().putString(KEYS_KEY, allKeys.toString()).apply();
            
            JSObject ret = new JSObject();
            ret.put("success", true);
            call.resolve(ret);
            
        } catch (Exception e) {
            call.reject("Failed to delete key: " + e.getMessage());
        }
    }

    @PluginMethod
    public void connectWithSecureKey(PluginCall call) {
        String hostname = call.getString("hostname");
        Integer port = call.getInt("port", 22);
        String username = call.getString("username");
        String keyId = call.getString("keyId");

        Log.d("SSHJPlugin", "connectWithSecureKey called for " + username + "@" + hostname + " with keyId: " + keyId);

        if (hostname == null || username == null || keyId == null) {
            call.reject("Missing required parameters");
            return;
        }

        try {
            // 1. Load key metadata FIRST to get the alias and IV
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String keysJson = prefs.getString(KEYS_KEY, "{}");
            JSONObject allKeys = new JSONObject(keysJson);
            
            if (!allKeys.has(keyId)) {
                Log.e("SSHJPlugin", "Key not found in SharedPreferences: " + keyId);
                call.reject("Key not found");
                return;
            }
            
            JSONObject keyMeta = allKeys.getJSONObject(keyId);
            String alias = keyMeta.getString("alias");
            String encryptedKey = keyMeta.getString("encryptedPrivateKey");
            String iv = keyMeta.getString("iv");

            Log.d("SSHJPlugin", "Key metadata loaded. Alias: " + alias + ", IV length: " + iv.length());

            // 2. Initialize Cipher for decryption BEFORE biometric prompt
            // This Cipher will be wrapped in a CryptoObject and authenticated
            Cipher cipher = secureKeyManager.getDecryptCipher(alias, iv);

            // 3. Show biometric prompt with the Cipher
            getActivity().runOnUiThread(() -> {
                BiometricHelper.authenticate(
                    getActivity(),
                    cipher, // Pass the cipher to be authenticated
                    "SSH Authentication",
                    "Authenticate to use your SSH key",
                    "Use " + username + "@" + hostname,
                    new BiometricHelper.AuthenticationCallback() {
                        @Override
                        public void onSuccess(Cipher authenticatedCipher) {
                            Log.d("SSHJPlugin", "Biometric authentication successful. Cipher authenticated.");
                            
                            // 4. Biometric success - use the AUTHENTICATED cipher to decrypt
                            new Thread(() -> {
                                SSHClient ssh = null;
                                java.io.File tempKeyFile = null;
                                try {
                                    // Decrypt private key using the authenticated cipher
                                    Log.d("SSHJPlugin", ">>> Decrypting private key from secure storage...");
                                    String privateKeyPem = secureKeyManager.decryptDataWithCipher(encryptedKey, authenticatedCipher);
                                    Log.d("SSHJPlugin", ">>> Private key decrypted successfully.");
                                    
                                    // Connect with SSH
                                    Log.d("SSHJPlugin", ">>> Initializing SSH client...");
                                    net.schmizz.sshj.DefaultConfig config = new net.schmizz.sshj.DefaultConfig();
                                    java.security.Security.removeProvider("BC");
                                    java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
                                    
                                    ssh = new SSHClient(config);
                                    ssh.addHostKeyVerifier(new PromiscuousVerifier());
                                    ssh.setTimeout(10000);
                                    ssh.setConnectTimeout(10000);
                                    
                                    Log.d("SSHJPlugin", ">>> Opening connection to " + hostname + ":" + port + "...");
                                    Log.d("SSHJPlugin", ">>> TCP connection timeout: 10000ms");
                                    ssh.connect(hostname, port);
                                    Log.d("SSHJPlugin", ">>> TCP connection established");
                                    Log.d("SSHJPlugin", ">>> SSH protocol handshake completed");
                                    
                                    // Log connection info
                                    try {
                                        String remoteHost = ssh.getRemoteHostname();
                                        int remotePort = ssh.getRemotePort();
                                        Log.d("SSHJPlugin", ">>> Connected to: " + remoteHost + ":" + remotePort);
                                    } catch (Exception e) {
                                        Log.d("SSHJPlugin", ">>> Connection details unavailable");
                                    }
                                    
                                    Log.d("SSHJPlugin", ">>> Preparing public key authentication for user: " + username);
                                    
                                    // Load key from memory (temporary file)
                                    tempKeyFile = java.io.File.createTempFile("ssh_key_temp_", ".pem");
                                    // Ensure file is only readable by owner
                                    tempKeyFile.setReadable(false, false);
                                    tempKeyFile.setReadable(true, true);
                                    
                                    java.io.FileWriter writer = new java.io.FileWriter(tempKeyFile);
                                    writer.write(privateKeyPem);
                                    writer.close();
                                    
                                    Log.d("SSHJPlugin", ">>> Loading private key...");
                                    KeyProvider keyProvider = ssh.loadKeys(tempKeyFile.getAbsolutePath());
                                    Log.d("SSHJPlugin", ">>> Key type: " + keyProvider.getType());
                                    
                                    Log.d("SSHJPlugin", ">>> Attempting public key authentication...");
                                    ssh.authPublickey(username, keyProvider);
                                    Log.d("SSHJPlugin", ">>> Public key authentication successful!");
                                    Log.d("SSHJPlugin", ">>> User " + username + " authenticated");
                                    
                                    // CRITICAL: Clear decrypted key from memory
                                    privateKeyPem = null;
                                    
                                    // Store session
                                    String sessionId = java.util.UUID.randomUUID().toString();
                                    activeSessions.put(sessionId, ssh);
                                    
                                    Log.d("SSHJPlugin", ">>> SSH session established");
                                    Log.d("SSHJPlugin", ">>> Session ID: " + sessionId);
                                    
                                    JSObject ret = new JSObject();
                                    ret.put("success", true);
                                    ret.put("sessionId", sessionId);
                                    ret.put("message", "Connected successfully with secure key");
                                    call.resolve(ret);
                                    
                                } catch (Exception e) {
                                    Log.e("SSHJPlugin", ">>> Connection failed: " + e.getMessage(), e);
                                    if (ssh != null) {
                                        try { 
                                            Log.d("SSHJPlugin", ">>> Closing connection...");
                                            ssh.disconnect(); 
                                        } catch (Exception ignored) {}
                                    }
                                    call.reject("Connection failed: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                                } finally {
                                    // Always clean up temp file
                                    if (tempKeyFile != null && tempKeyFile.exists()) {
                                        tempKeyFile.delete();
                                    }
                                }
                            }).start();
                        }

                        @Override
                        public void onError(String message) {
                            Log.e("SSHJPlugin", "Biometric error: " + message);
                            call.reject("Biometric authentication failed: " + message);
                        }

                        @Override
                        public void onCancel() {
                            Log.d("SSHJPlugin", "Biometric canceled");
                            call.reject("Biometric authentication canceled");
                        }
                    }
                );
            });

        } catch (Exception e) {
            Log.e("SSHJPlugin", "Setup failed", e);
            call.reject("Failed to initiate secure connection: " + e.getMessage());
        }
    }

    private String convertPrivateKeyToPEM(java.security.PrivateKey privateKey, String keyType) throws Exception {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN OPENSSH PRIVATE KEY-----\n");
        
        byte[] encoded = privateKey.getEncoded();
        String base64 = android.util.Base64.encodeToString(encoded, android.util.Base64.NO_WRAP);
        
        // Split into 64-char lines
        int index = 0;
        while (index < base64.length()) {
            int endIndex = Math.min(index + 64, base64.length());
            pem.append(base64.substring(index, endIndex)).append("\n");
            index = endIndex;
        }
        
        pem.append("-----END OPENSSH PRIVATE KEY-----\n");
        return pem.toString();
    }

    private String convertPublicKeyToOpenSSH(java.security.PublicKey publicKey, String keyType, String comment) throws Exception {
        byte[] encoded = publicKey.getEncoded();
        String base64 = android.util.Base64.encodeToString(encoded, android.util.Base64.NO_WRAP);
        
        String prefix = "rsa".equalsIgnoreCase(keyType) ? "ssh-rsa" : "ssh-ed25519";
        String commentPart = comment.isEmpty() ? "" : " " + comment;
        
        return prefix + " " + base64 + commentPart;
    }
}
