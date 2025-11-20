<script setup lang="ts">
import { Page } from 'konsta/vue'
import type { SSHHost } from '~/stores/ssh'
import AuthMethodSelector from '~/components/ssh/AuthMethodSelector.vue'
import SecureKeySelector from '~/components/ssh/SecureKeySelector.vue'
import PasswordInput from '~/components/ssh/PasswordInput.vue'
import BasicInfoForm from '~/components/ssh/BasicInfoForm.vue'

const router = useRouter()
const sshStore = useSSHStore()
const keyStore = useKeyStore()

// Load stored keys
onMounted(() => {
  keyStore.loadKeys()
})

// Form state
const formData = reactive<Partial<SSHHost>>({
  name: '',
  hostname: '',
  port: 22,
  username: '',
  authMethod: 'secureKey',
  password: '',
  secureKeyId: '',
  secureKeyLabel: '',
  description: '',
  tags: [],
  forwardAgent: false,
  compression: false,
  keepAlive: true,
  strictHostKeyChecking: true,
  proxyJump: '',
  localForwards: [],
  remoteForwards: [],
})

// Local computed for proper reactivity
const storedKeys = computed(() => keyStore.storedKeys.value || [])

// Validation
const isValid = computed(() => {
  return formData.name?.trim() !== '' &&
         formData.hostname?.trim() !== '' &&
         formData.username?.trim() !== '' &&
         (formData.port || 0) > 0 && (formData.port || 0) <= 65535
})

// Save
const handleSave = () => {
  if (!isValid.value) return
  
  const hostData = { ...formData }
  
  // Clean up based on auth method
  if (hostData.authMethod !== 'password') {
    delete hostData.password
  }
  if (hostData.authMethod !== 'secureKey') {
    delete hostData.secureKeyId
    delete hostData.secureKeyLabel
  }
  
  sshStore.addHost(hostData as SSHHost)
  router.push('/ssh')
}
</script>

<template>
    <div class="max-w-2xl mx-auto pb-24">
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-2xl font-bold text-slate-200 mb-1">New SSH Host</h1>
        <p class="text-slate-400 text-sm">Configure your SSH connection</p>
      </div>

      <div class="space-y-6">
        <!-- Basic Info -->
        <BasicInfoForm :formData="formData" @update:formData="Object.assign(formData, $event)" />

        <!-- Authentication -->
        <div>
          <h2 class="text-lg font-semibold text-slate-200 mb-4">Authentication</h2>
          <div class="space-y-4">
            <AuthMethodSelector v-model="formData.authMethod" :keyCount="storedKeys.length" />
            
            <SecureKeySelector
              v-if="formData.authMethod === 'secureKey'"
              :keys="storedKeys"
              :selectedKeyId="formData.secureKeyId"
              @update:selectedKeyId="formData.secureKeyId = $event"
              @update:selectedKeyLabel="formData.secureKeyLabel = $event"
            />
            
            <PasswordInput
              v-if="formData.authMethod === 'password'"
              v-model="formData.password"
            />
          </div>
        </div>

        <!-- Advanced Options -->
        <div>
          <h2 class="text-lg font-semibold text-slate-200 mb-4">Advanced Options</h2>
          <div class="card p-4 flex items-center justify-between">
            <div>
              <div class="font-medium text-slate-200">Keep Alive</div>
              <div class="text-xs text-slate-400">Send keep-alive packets</div>
            </div>
            <button
              :class="['w-14 h-8 rounded-full transition-all', formData.keepAlive ? 'bg-blue-600' : 'bg-slate-700']"
              @click="formData.keepAlive = !formData.keepAlive"
            >
              <div :class="['w-6 h-6 rounded-full bg-white transition-transform', formData.keepAlive ? 'translate-x-7' : 'translate-x-1']" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Fixed Bottom Actions -->
    <div class="fixed bottom-0 left-0 right-0 p-4 safe-area-bottom">
      <div class="flex gap-3 max-w-2xl mx-auto">
        <button 
          class="flex-1 py-3 rounded-xl font-medium bg-slate-700 text-slate-200 active:scale-95 transition-transform" 
          @click="router.back()"
        >
          Cancel
        </button>
        <button
          :disabled="!isValid"
          :class="['flex-1 btn-primary', !isValid && 'opacity-50 cursor-not-allowed']"
          @click="handleSave"
        >
          <Icon name="lucide:plus" class="w-5 h-5" />
          Create Host
        </button>
      </div>
    </div>
</template>
