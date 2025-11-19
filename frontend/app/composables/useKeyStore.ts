import { ref, computed } from 'vue'

export const useKeyStore = () => {
    const { $ssh } = useNuxtApp()

    const storedKeys = ref<Array<{
        keyId: string
        label: string
        publicKey: string
        keyType: string
        createdAt: string
    }>>([])

    const loading = ref(false)
    const error = ref<string | null>(null)

    /**
     * Load all stored SSH keys from native storage
     */
    const loadKeys = async () => {
        loading.value = true
        error.value = null
        try {
            console.log('[useKeyStore] Calling listStoredKeys...')
            const result = await $ssh.listStoredKeys()
            console.log('[useKeyStore] Result:', result)
            console.log('[useKeyStore] Result JSON:', JSON.stringify(result, null, 2))
            console.log('[useKeyStore] Result.keys type:', typeof result.keys)
            console.log('[useKeyStore] Result.keys Array.isArray:', Array.isArray(result.keys))

            // Handle different response formats
            if (result.keys && Array.isArray(result.keys)) {
                storedKeys.value = result.keys
                console.log('[useKeyStore] Loaded', result.keys.length, 'keys (was array)')
            } else if (result.keys) {
                // JSArray from Capacitor appears as object with numeric keys
                const keysArray = Object.values(result.keys).filter(v => typeof v === 'object')
                storedKeys.value = keysArray as any
                console.log('[useKeyStore] Converted to array:', keysArray.length, 'keys')
                console.log('[useKeyStore] First key:', JSON.stringify(keysArray[0], null, 2))
            } else {
                storedKeys.value = []
                console.log('[useKeyStore] No keys found')
            }
        } catch (err: any) {
            error.value = err.message || 'Failed to load keys'
            console.error('[useKeyStore] Failed to load keys:', err)
            storedKeys.value = []
        } finally {
            loading.value = false
            console.log('[useKeyStore] Final storedKeys.value:', storedKeys.value)
            console.log('[useKeyStore] Final storedKeys.value length:', storedKeys.value.length)
        }
    }

    /**
     * Generate a new secure SSH key pair
     * Private key is stored in hardware-backed KeyStore, never exposed
     */
    const generateKey = async (label: string, keyType: string = 'rsa', keySize: number = 2048) => {
        loading.value = true
        error.value = null
        try {
            const result = await $ssh.generateSecureKeyPair({
                label,
                keyType,
                keySize
            })

            if (result.success) {
                await loadKeys() // Refresh list
                return result
            } else {
                throw new Error('Key generation failed')
            }
        } catch (err: any) {
            error.value = err.message || 'Failed to generate key'
            console.error('Failed to generate key:', err)
            throw err
        } finally {
            loading.value = false
        }
    }

    /**
     * Delete a stored SSH key
     * Removes from both KeyStore and metadata
     */
    const deleteKey = async (keyId: string) => {
        loading.value = true
        error.value = null
        try {
            await $ssh.deleteSecureKey({ keyId })
            await loadKeys() // Refresh list
        } catch (err: any) {
            error.value = err.message || 'Failed to delete key'
            console.error('Failed to delete key:', err)
            throw err
        } finally {
            loading.value = false
        }
    }

    /**
     * Get fingerprint (shortened public key) for display
     */
    const getFingerprint = (publicKey: string): string => {
        if (!publicKey) return ''
        // Extract the key part (remove prefix and comment)
        const parts = publicKey.split(' ')
        const keyPart = parts[1] || publicKey

        // Show first and last characters
        if (keyPart.length > 20) {
            return `${keyPart.substring(0, 8)}...${keyPart.substring(keyPart.length - 8)}`
        }
        return keyPart
    }

    return {
        storedKeys,
        loading,
        error,
        loadKeys,
        generateKey,
        deleteKey,
        getFingerprint
    }
}
