<script setup lang="ts">
import { Page } from 'konsta/vue'

const router = useRouter()
const keyStore = useKeyStore()

const showAddKey = ref(false)
const generating = ref(false)
const keyType = ref('ed25519')
const keySize = ref(4096)
const label = ref('')

onMounted(() => {
  keyStore.loadKeys()
})

const keys = computed(() => keyStore.storedKeys.value || [])
const isLoading = computed(() => keyStore.loading.value)

const handleGenerate = async () => {
  if (!label.value.trim()) {
    alert('Please enter a label for your key')
    return
  }
  
  generating.value = true
  try {
    await keyStore.generateKey(
      label.value,
      keyType.value,
      keyType.value === 'rsa' ? keySize.value : undefined
    )
    
    // Reset form and close modal
    label.value = ''
    showAddKey.value = false
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
</script>

<template>
    <div class="max-w-2xl mx-auto">
      <!-- Security Notice -->
      <div class="mb-6 p-4 rounded-xl bg-blue-500/10 border border-blue-500/20">
        <div class="flex items-start gap-3">
          <Icon name="lucide:shield-check" class="w-5 h-5 text-blue-400 mt-0.5 flex-shrink-0" />
          <div class="text-sm">
            <p class="font-medium text-blue-300 mb-1">🔒 Hardware-Protected Keys</p>
            <p class="text-blue-400/80">
              Keys are stored in your device's secure hardware and protected by biometric authentication.
            </p>
          </div>
        </div>
      </div>

      <!-- Keys List -->
      <div v-if="isLoading" class="text-center py-16">
        <Icon name="lucide:loader-2" class="w-8 h-8 mx-auto mb-2 animate-spin text-blue-400" />
        <p class="text-slate-400 text-sm">Loading keys...</p>
      </div>

      <div v-else-if="keys.length === 0" class="text-center py-16">
        <div class="w-20 h-20 rounded-full bg-slate-800/50 mx-auto mb-6 flex items-center justify-center">
          <Icon name="lucide:key-off" class="w-10 h-10 text-slate-600" />
        </div>
        <h3 class="text-xl font-semibold text-slate-300 mb-2">No SSH Keys</h3>
        <p class="text-slate-500 mb-6">Generate your first secure SSH key</p>
        <button
          class="neomorph-btn px-8 py-3 rounded-xl font-medium text-gray-200 inline-flex items-center gap-2"
          @click="showAddKey = true"
        >
          <Icon name="lucide:plus" class="w-5 h-5" />
          Generate Key
        </button>
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="key in keys"
          :key="key.keyId"
          class="card p-4"
        >
          <div class="flex items-start justify-between mb-3">
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <div class="w-12 h-12 flex items-center justify-center rounded-full bg-blue-500/10 text-blue-400 flex-shrink-0">
                <Icon name="lucide:key" class="w-5 h-5" />
              </div>
              <div class="truncate">
                <h3 class="font-semibold text-slate-200 truncate">{{ key.label }}</h3>
                <div class="flex items-center gap-2 text-xs text-slate-500">
                  <span :class="key.keyType === 'rsa' ? 'text-blue-400' : 'text-green-400'">
                    {{ key.keyType.toUpperCase() }}
                  </span>
                  <span>•</span>
                  <span>{{ new Date(key.createdAt).toLocaleDateString() }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="bg-slate-900/50 p-3 rounded-lg mb-3">
            <p class="text-xs text-slate-400 mb-1">Fingerprint:</p>
            <pre class="text-xs font-mono text-slate-300 overflow-x-auto">{{ keyStore.getFingerprint(key.publicKey) }}</pre>
          </div>

          <div class="flex gap-2">
            <button
              class="flex-1 py-2 px-4 rounded-lg bg-blue-600 text-white text-sm font-medium active:scale-95 transition-transform flex items-center justify-center gap-2"
              @click="copyPublicKey(key.publicKey)"
            >
              <Icon name="lucide:copy" class="w-4 h-4" />
              Copy Public Key
            </button>
            <button
              class="p-2 rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition-colors"
              @click="handleDelete(key.keyId, key.label)"
            >
              <Icon name="lucide:trash-2" class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <!-- FAB -->
      <button
        v-if="keys.length > 0"
        class="fixed bottom-6 right-6 w-14 h-14 rounded-full bg-blue-600 text-white shadow-2xl shadow-blue-900/30 active:scale-95 transition-all z-40 flex items-center justify-center"
        @click="showAddKey = true"
      >
        <Icon name="lucide:plus" class="w-6 h-6" />
      </button>
    </div>

    <!-- Add Key Modal -->
    <Teleport to="body">
      <div
        v-if="showAddKey"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70"
        @click.self="showAddKey = false"
      >
        <div class="card max-w-md w-full p-6">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-bold text-slate-200">Generate SSH Key</h2>
            <button
              class="p-2 rounded-full hover:bg-slate-700/50 transition-colors"
              @click="showAddKey = false"
            >
              <Icon name="lucide:x" class="w-5 h-5 text-slate-400" />
            </button>
          </div>

          <div class="space-y-4">
            <!-- Label -->
            <div>
              <label class="block text-sm font-medium text-slate-300 mb-2">
                Key Label <span class="text-red-400">*</span>
              </label>
              <input
                v-model="label"
                type="text"
                placeholder="e.g. Work Server, Personal VPS"
                class="input-field"
              />
            </div>

            <!-- Key Type -->
            <div>
              <label class="block text-sm font-medium text-slate-300 mb-3">
                Key Type
              </label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  :class="[
                    'p-4 rounded-xl text-left transition-all border',
                    keyType === 'ed25519' 
                      ? 'border-blue-500 bg-blue-500/10' 
                      : 'border-slate-700 bg-slate-800/50'
                  ]"
                  @click="keyType = 'ed25519'"
                >
                  <div class="flex items-center gap-3">
                    <Icon name="lucide:shield" class="w-6 h-6 text-green-400" />
                    <div>
                      <div class="font-medium text-slate-200">Ed25519</div>
                      <div class="text-xs text-slate-400">Recommended</div>
                    </div>
                  </div>
                </button>

                <button
                  :class="[
                    'p-4 rounded-xl text-left transition-all border',
                    keyType === 'rsa' 
                      ? 'border-blue-500 bg-blue-500/10' 
                      : 'border-slate-700 bg-slate-800/50'
                  ]"
                  @click="keyType = 'rsa'"
                >
                  <div class="flex items-center gap-3">
                    <Icon name="lucide:key" class="w-6 h-6 text-blue-400" />
                    <div>
                      <div class="font-medium text-slate-200">RSA</div>
                      <div class="text-xs text-slate-400">Compatible</div>
                    </div>
                  </div>
                </button>
              </div>
            </div>

            <!-- Key Size (RSA only) -->
            <div v-if="keyType === 'rsa'">
              <label class="block text-sm font-medium text-slate-300 mb-2">
                Key Size
              </label>
              <select
                v-model.number="keySize"
                class="input-field"
              >
                <option value="2048">2048 bits (Standard)</option>
                <option value="4096">4096 bits (More Secure)</option>
              </select>
            </div>

            <!-- Generate Button -->
            <button
              :disabled="generating || !label.trim()"
              class="btn-primary w-full"
              :class="generating || !label.trim() ? 'opacity-50' : ''"
              @click="handleGenerate"
            >
              <Icon v-if="generating" name="lucide:loader-2" class="w-5 h-5 animate-spin" />
              <Icon v-else name="lucide:sparkles" class="w-5 h-5" />
              {{ generating ? 'Generating...' : 'Generate Secure Key' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
</template>
