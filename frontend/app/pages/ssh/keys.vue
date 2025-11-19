<script setup lang="ts">
import { Page } from 'konsta/vue'

const router = useRouter()
const keyStore = useKeyStore()

const generating = ref(false)
const keyType = ref('rsa')
const keySize = ref(2048)
const label = ref('')
const showSuccess = ref(false)
const generatedKeyInfo = ref<{ keyId: string; publicKey: string; label: string } | null>(null)

onMounted(() => {
  keyStore.loadKeys()
})

// Debug: watch storedKeys changes
watch(() => keyStore.storedKeys, (newKeys) => {
  console.log('[keys.vue] storedKeys changed:', newKeys)
  console.log('[keys.vue] storedKeys length:', newKeys?.length)
  console.log('[keys.vue] First key:', newKeys?.[0])
}, { immediate: true, deep: true })

// Local computed for proper reactivity
const keys = computed(() => keyStore.storedKeys.value || [])
const isLoading = computed(() => keyStore.loading.value)

const handleGenerate = async () => {
  if (!label.value.trim()) {
    alert('Please enter a label for your key')
    return
  }
  
  generating.value = true
  try {
    const result = await keyStore.generateKey(
      label.value,
      keyType.value,
      keyType.value === 'rsa' ? keySize.value : undefined
    )
    
    generatedKeyInfo.value = {
      keyId: result.keyId,
      publicKey: result.publicKey,
      label: result.label
    }
    showSuccess.value = true
    
    // Reset form
    label.value = ''
  } catch (error: any) {
    alert('Failed to generate key: ' + error.message)
  } finally {
    generating.value = false
  }
}

const handleDelete = async (keyId: string, keyLabel: string) => {
  if (confirm(`Delete key "${keyLabel}"? This cannot be undone.`)) {
    try {
      await keyStore.deleteKey(keyId)
    } catch (error: any) {
      alert('Failed to delete key: ' + error.message)
    }
  }
}

const copyPublicKey = (publicKey: string) => {
  navigator.clipboard.writeText(publicKey)
  alert('Public key copied to clipboard!')
}

const resetForm = () => {
  showSuccess.value = false
  generatedKeyInfo.value = null
}
</script>

<template>
  <Page>
    <Breadcrumbs />
    
    <div class="min-h-screen pb-6 px-4 py-6">
      <!-- Header -->
      <div class="mb-6">
        <div class="flex items-center gap-3 mb-2">
          <button
            class="neomorph-btn p-2 rounded-lg"
            @click="router.back()"
          >
            <Icon name="lucide:arrow-left" class="w-5 h-5 text-gray-300" />
          </button>
          <h1 class="text-3xl font-bold text-gray-100">
            Secure SSH Keys
          </h1>
        </div>
        <p class="text-gray-400 ml-14">Hardware-protected key storage</p>
      </div>

      <!-- Security Notice -->
      <div class="mb-6 p-4 rounded-xl bg-blue-500/10 border border-blue-500/20">
        <div class="flex items-start gap-3">
          <Icon name="lucide:shield-check" class="w-5 h-5 text-blue-400 mt-0.5 flex-shrink-0" />
          <div class="text-sm">
            <p class="font-medium text-blue-300 mb-1">🔒 Maximum Security</p>
            <p class="text-blue-400/80">
              Private keys are stored in your device's secure hardware (KeyStore) and protected by biometric authentication. They never leave your device and cannot be copied.
            </p>
          </div>
        </div>
      </div>

      <!-- Success Message -->
      <div v-if="showSuccess && generatedKeyInfo" class="mb-6">
        <div class="neomorph p-6 rounded-2xl text-center">
          <div class="w-20 h-20 mx-auto mb-4 neomorph-pressed rounded-full flex items-center justify-center">
            <Icon name="lucide:check-circle" class="w-10 h-10 text-green-400" />
          </div>
          <h3 class="text-xl font-bold text-gray-100 mb-2">Key Generated!</h3>
          <p class="text-gray-400 text-sm mb-4">"{{ generatedKeyInfo.label }}" is ready to use</p>
          
          <!-- Public Key Display -->
          <div class="neomorph-pressed p-3 rounded-lg mb-4">
            <p class="text-xs text-gray-400 mb-2">Public Key:</p>
            <div class="flex items-center gap-2">
              <pre class="flex-1 text-xs font-mono text-gray-300 overflow-x-auto">{{ keyStore.getFingerprint(generatedKeyInfo.publicKey) }}</pre>
              <button
                class="neomorph-btn p-2 rounded-lg flex-shrink-0"
                @click="copyPublicKey(generatedKeyInfo.publicKey)"
              >
                <Icon name="lucide:copy" class="w-4 h-4 text-gray-300" />
              </button>
            </div>
          </div>
          
          <button
            class="neomorph-btn py-3 px-6 rounded-xl font-medium text-gray-200"
            @click="resetForm"
          >
            Generate Another Key
          </button>
        </div>
      </div>

      <!-- Generator Form -->
      <div v-if="!showSuccess" class="space-y-4 mb-6">
        <!-- Key Label -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">
            Key Label <span class="text-red-400">*</span>
          </label>
          <div class="neomorph-pressed rounded-xl overflow-hidden">
            <input
              v-model="label"
              type="text"
              placeholder="e.g. Work Server, Personal VPS"
              class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
            />
          </div>
          <p class="text-xs text-gray-400 mt-2">
            Give this key a memorable name
          </p>
        </div>

        <!-- Key Type -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-3">
            Key Type
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button
              :class="[
                'p-4 rounded-xl text-left transition-all',
                keyType === 'rsa' ? 'neomorph' : 'neomorph-pressed'
              ]"
              @click="keyType = 'rsa'"
            >
              <div class="flex items-center gap-3">
                <Icon name="lucide:key" class="w-6 h-6 text-blue-400" />
                <div>
                  <div class="font-medium text-gray-200">RSA</div>
                  <div class="text-xs text-gray-400">Most compatible</div>
                </div>
              </div>
            </button>

            <button
              :class="[
                'p-4 rounded-xl text-left transition-all',
                keyType === 'ed25519' ? 'neomorph' : 'neomorph-pressed'
              ]"
              @click="keyType = 'ed25519'"
            >
              <div class="flex items-center gap-3">
                <Icon name="lucide:shield" class="w-6 h-6 text-green-400" />
                <div>
                  <div class="font-medium text-gray-200">Ed25519</div>
                  <div class="text-xs text-gray-400">More secure</div>
                </div>
              </div>
            </button>
          </div>
        </div>

        <!-- Key Size (RSA only) -->
        <div v-if="keyType === 'rsa'">
          <label class="block text-sm font-medium text-gray-300 mb-2">
            Key Size
          </label>
          <div class="neomorph-pressed rounded-xl overflow-hidden">
            <select
              v-model.number="keySize"
              class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
            >
              <option value="2048">2048 bits (Standard)</option>
              <option value="4096">4096 bits (More Secure)</option>
            </select>
          </div>
        </div>

        <!-- Generate Button -->
        <button
          :disabled="generating || !label.trim()"
          class="w-full py-4 rounded-xl font-medium text-white flex items-center justify-center gap-2"
          :class="generating || !label.trim() ? 'opacity-50' : ''"
          style="background: linear-gradient(145deg, #3b82f6, #2563eb); box-shadow: 6px 6px 12px rgba(0, 0, 0, 0.4), -6px -6px 12px rgba(59, 130, 246, 0.1);"
          @click="handleGenerate"
        >
          <Icon v-if="generating" name="lucide:loader-2" class="w-5 h-5 animate-spin" />
          <Icon v-else name="lucide:sparkles" class="w-5 h-5" />
          {{ generating ? 'Generating...' : 'Generate Secure Key' }}
        </button>
      </div>

      <!-- Stored Keys List -->
      <div>
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold text-gray-200 flex items-center gap-2">
            <Icon name="lucide:key" class="w-5 h-5 text-blue-400" />
            Stored Keys ({{ keys.length }})
          </h2>
          <button
            v-if="keys.length > 0"
            class="neomorph-btn p-2 rounded-lg"
            @click="keyStore.loadKeys()"
          >
            <Icon name="lucide:refresh-cw" class="w-4 h-4 text-gray-300" />
          </button>
        </div>

        <!-- Loading State -->
        <div v-if="isLoading" class="text-center py-8">
          <Icon name="lucide:loader-2" class="w-8 h-8 mx-auto mb-2 animate-spin text-blue-400" />
          <p class="text-gray-400 text-sm">Loading keys...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="keys.length === 0" class="text-center py-12">
          <div class="neomorph-pressed w-20 h-20 rounded-full mx-auto mb-4 flex items-center justify-center">
            <Icon name="lucide:key-off" class="w-10 h-10 text-gray-500" />
          </div>
          <h3 class="text-lg font-semibold text-gray-300 mb-1">No Keys Yet</h3>
          <p class="text-gray-500 text-sm">Generate your first secure SSH key above</p>
        </div>

        <!-- Keys List -->
        <div v-else class="space-y-3">
          <div
            v-for="key in keys"
            :key="key.keyId"
            class="neomorph p-4 rounded-xl"
          >
            <div class="flex items-start justify-between mb-3">
              <div class="flex-1">
                <h3 class="font-semibold text-gray-200 mb-1">{{ key.label }}</h3>
                <div class="flex items-center gap-2 text-xs text-gray-400">
                  <span :class="key.keyType === 'rsa' ? 'text-blue-400' : 'text-green-400'">
                    {{ key.keyType.toUpperCase() }}
                  </span>
                  <span>•</span>
                  <span>{{ new Date(key.createdAt).toLocaleDateString() }}</span>
                </div>
              </div>
              <button
                class="neomorph-pressed p-2 rounded-lg text-red-400"
                @click="handleDelete(key.keyId, key.label)"
              >
                <Icon name="lucide:trash-2" class="w-4 h-4" />
              </button>
            </div>
            
            <div class="neomorph-pressed p-3 rounded-lg">
              <div class="flex items-center justify-between mb-1">
                <p class="text-xs text-gray-400">Public Key Fingerprint:</p>
                <button
                  class="text-xs text-blue-400 hover:text-blue-300"
                  @click="copyPublicKey(key.publicKey)"
                >
                  Copy Full Key
                </button>
              </div>
              <pre class="text-xs font-mono text-gray-300">{{ keyStore.getFingerprint(key.publicKey) }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Page>
</template>
