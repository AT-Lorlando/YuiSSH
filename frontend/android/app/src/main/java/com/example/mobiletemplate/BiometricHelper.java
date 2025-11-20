package com.example.mobiletemplate;

import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * Helper class for biometric authentication
 */
public class BiometricHelper {
    
    public interface AuthenticationCallback {
        void onSuccess(javax.crypto.Cipher cipher);
        void onError(String message);
        void onCancel();
    }

    /**
     * Check if biometric authentication is available
     */
    public static boolean canAuthenticate(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        );
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Show biometric prompt (simple auth without crypto)
     */
    public static void authenticate(
        FragmentActivity activity,
        String title,
        String subtitle,
        String description,
        AuthenticationCallback callback
    ) {
        authenticate(activity, null, title, subtitle, description, callback);
    }

    /**
     * Show biometric prompt with optional CryptoObject
     */
    public static void authenticate(
        FragmentActivity activity,
        javax.crypto.Cipher cipher,
        String title,
        String subtitle,
        String description,
        AuthenticationCallback callback
    ) {
        Executor executor = ContextCompat.getMainExecutor(activity);
        
        BiometricPrompt biometricPrompt = new BiometricPrompt(
            activity,
            executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    javax.crypto.Cipher authenticatedCipher = null;
                    if (result.getCryptoObject() != null) {
                        authenticatedCipher = result.getCryptoObject().getCipher();
                    }
                    callback.onSuccess(authenticatedCipher);
                }

                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        callback.onCancel();
                    } else {
                        callback.onError(errString.toString());
                    }
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    // Don't call callback here - user can retry
                }
            }
        );

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build();

        if (cipher != null) {
            BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(cipher);
            biometricPrompt.authenticate(promptInfo, cryptoObject);
        } else {
            biometricPrompt.authenticate(promptInfo);
        }
    }
}
