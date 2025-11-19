<script setup lang="ts">
import { Page } from 'konsta/vue'
import type { SSHHost } from '~/stores/ssh'
import AuthMethodSelector from '~/components/ssh/AuthMethodSelector.vue'
import SecureKeySelector from '~/components/ssh/SecureKeySelector.vue'
import PasswordInput from '~/components/ssh/PasswordInput.vue'
import BasicInfoForm from '~/components/ssh/BasicInfoForm.vue'

const router = useRouter()
const route = useRoute()
const sshStore = useSSHStore()
const keyStore = useKeyStore()

const hostId = route.params.id as string

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

// Load host and keys on mount
onMounted(() => {
  keyStore.loadKeys()
  sshStore.loadHosts()
  const host = sshStore.getHostById(hostId)
  if (!host) {
    router.push('/ssh')
    return
  }
  
  // Populate form with existing data
  Object.assign(formData, {
    name: host.name,
    hostname: host.hostname,
    port: host.port,
    username: host.username,
    authMethod: host.authMethod,
    password: host.password || '',
    secureKeyId: host.secureKeyId || '',
    secureKeyLabel: host.secureKeyLabel || '',
    description: host.description || '',
    tags: host.tags || [],
    forwardAgent: host.forwardAgent || false,
    compression: host.compression || false,
    keepAlive: host.keepAlive !== undefined ? host.keepAlive : true,
    strictHostKeyChecking: host.strictHostKeyChecking !== undefined ? host.strictHostKeyChecking : true,
    proxyJump: host.proxyJump || '',
    localForwards: host.localForwards || [],
    remoteForwards: host.remoteForwards || [],
  })
})

// UI state
const currentSection = ref<'basic' | 'auth' | 'advanced'>('basic')

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
  
  const updates = { ...formData }
  
  // Clean up based on auth method
  if (updates.authMethod !== 'password') {
    delete updates.password
  }
  if (updates.authMethod !== 'secureKey') {
    delete updates.secureKeyId
    delete updates.secureKeyLabel
  }
  
  sshStore.updateHost(hostId, updates)
  router.push('/ssh')
}

const handleCancel = () => {
  router.back()
}
</script>

<template>
  <Page>
    <Breadcrumbs />
    
    <div class="min-h-screen px-4 py-6 pb-24">
      <!-- Header -->
      <div class="mb-6">
        <div class="flex items-center gap-3 mb-2">
          <button class="neomorph-btn p-2 rounded-lg" @click="handleCancel">
            <Icon name="lucide:arrow-left" class="w-5 h-5 text-gray-300" />
          </button>
          <h1 class="text-3xl font-bold text-gray-100">Edit SSH Host</h1>
        </div>
        <p class="text-gray-400 ml-14">Update your SSH connection settings</p>
      </div>

      <!-- Section Navigation -->
      <div class="neomorph-pressed rounded-xl p-2 mb-6 grid grid-cols-3 gap-2">
        <button
          :class="['py-2 px-3 rounded-lg text-sm font-medium transition-all', currentSection === 'basic' ? 'neomorph text-gray-200' : 'text-gray-400']"
          @click="currentSection = 'basic'"
        >
          <Icon name="lucide:info" class="w-4 h-4 mx-auto mb-1" />
          <div class="text-xs">Basic</div>
        </button>
        <button
          :class="['py-2 px-3 rounded-lg text-sm font-medium transition-all', currentSection === 'auth' ? 'neomorph text-gray-200' : 'text-gray-400']"
          @click="currentSection = 'auth'"
        >
          <Icon name="lucide:key" class="w-4 h-4 mx-auto mb-1" />
          <div class="text-xs">Auth</div>
        </button>
        <button
          :class="['py-2 px-3 rounded-lg text-sm font-medium transition-all', currentSection === 'advanced' ? 'neomorph text-gray-200' : 'text-gray-400']"
          @click="currentSection = 'advanced'"
        >
          <Icon name="lucide:settings" class="w-4 h-4 mx-auto mb-1" />
          <div class="text-xs">Advanced</div>
        </button>
      </div>

      <!-- Basic Section -->
      <div v-show="currentSection === 'basic'">
        <BasicInfoForm :formData="formData" @update:formData="Object.assign(formData, $event)" />
      </div>

      <!-- Auth Section -->
      <div v-show="currentSection === 'auth'" class="space-y-4">
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

      <!-- Advanced Section -->
      <div v-show="currentSection === 'advanced'" class="space-y-3">
        <div class="neomorph p-4 rounded-xl flex items-center justify-between">
          <div>
            <div class="font-medium text-gray-200">Keep Alive</div>
            <div class="text-xs text-gray-400">Send keep-alive packets</div>
          </div>
          <button
            :class="['w-14 h-8 rounded-full transition-all', formData.keepAlive ? 'bg-blue-500' : 'neomorph-pressed']"
            @click="formData.keepAlive = !formData.keepAlive"
          >
            <div :class="['w-6 h-6 rounded-full bg-white transition-transform', formData.keepAlive ? 'translate-x-7' : 'translate-x-1']" />
          </button>
        </div>
      </div>
    </div>

    <!-- Fixed Bottom Actions -->
    <div class="fixed bottom-0 left-0 right-0 p-4 neomorph border-t border-gray-700">
      <div class="flex gap-3 max-w-2xl mx-auto">
        <button class="flex-1 neomorph-pressed py-3 rounded-xl font-medium text-gray-300" @click="handleCancel">
          Cancel
        </button>
        <button
          :disabled="!isValid"
          :class="['flex-2 py-3 px-6 rounded-xl font-medium text-white flex items-center justify-center gap-2', isValid ? 'neomorph-btn' : 'neomorph-pressed opacity-50 cursor-not-allowed']"
          @click="handleSave"
        >
          <Icon name="lucide:save" class="w-5 h-5" />
          Update Host
        </button>
      </div>
    </div>
  </Page>
</template>
