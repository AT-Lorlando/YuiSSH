<script setup lang="ts">
import { Page } from 'konsta/vue'
import { useSSHStore } from '~/stores/ssh'
import type { SSHHost, LocalForward, RemoteForward } from '~/stores/ssh'

const router = useRouter()
const route = useRoute()
const sshStore = useSSHStore()

const hostId = route.params.id as string

// Load host on mount
onMounted(() => {
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
    privateKey: host.privateKey || '',
    privateKeyPath: host.privateKeyPath || '',
    passphrase: host.passphrase || '',
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

// Form state
const formData = reactive({
  name: '',
  hostname: '',
  port: 22,
  username: '',
  authMethod: 'privateKey' as 'password' | 'privateKey' | 'agent',
  password: '',
  privateKey: '',
  privateKeyPath: '',
  passphrase: '',
  description: '',
  tags: [] as string[],
  forwardAgent: false,
  compression: false,
  keepAlive: true,
  strictHostKeyChecking: true,
  proxyJump: '',
  localForwards: [] as LocalForward[],
  remoteForwards: [] as RemoteForward[],
})

// UI state
const currentSection = ref<'basic' | 'auth' | 'advanced' | 'forwarding'>('basic')
const tagInput = ref('')

// Add tag
const addTag = () => {
  if (tagInput.value && !formData.tags.includes(tagInput.value)) {
    formData.tags.push(tagInput.value)
    tagInput.value = ''
  }
}

const removeTag = (tag: string) => {
  formData.tags = formData.tags.filter(t => t !== tag)
}

// Forwarding
const addLocalForward = () => {
  formData.localForwards.push({
    localPort: 8080,
    remoteHost: 'localhost',
    remotePort: 80,
  })
}

const removeLocalForward = (index: number) => {
  formData.localForwards.splice(index, 1)
}

const addRemoteForward = () => {
  formData.remoteForwards.push({
    remotePort: 8080,
    localHost: 'localhost',
    localPort: 80,
  })
}

const removeRemoteForward = (index: number) => {
  formData.remoteForwards.splice(index, 1)
}

// Validation
const isValid = computed(() => {
  return formData.name.trim() !== '' &&
         formData.hostname.trim() !== '' &&
         formData.username.trim() !== '' &&
         formData.port > 0 && formData.port <= 65535
})

// Save
const handleSave = () => {
  if (!isValid.value) return
  
  const updates = { ...formData }
  
  // Clean up based on auth method
  if (updates.authMethod !== 'password') {
    delete updates.password
  }
  if (updates.authMethod !== 'privateKey') {
    delete updates.privateKey
    delete updates.privateKeyPath
    delete updates.passphrase
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
          <button
            class="neomorph-btn p-2 rounded-lg"
            @click="handleCancel"
          >
            <Icon name="lucide:arrow-left" class="w-5 h-5 text-gray-300" />
          </button>
          <h1 class="text-3xl font-bold text-gray-100">
            Edit SSH Host
          </h1>
        </div>
        <p class="text-gray-400 ml-14">Update your SSH connection settings</p>
      </div>

      <!-- Section Navigation -->
      <div class="neomorph-pressed rounded-xl p-2 mb-6 grid grid-cols-4 gap-2">
        <button
          :class="[
            'py-2 px-3 rounded-lg text-sm font-medium transition-all',
            currentSection === 'basic' 
              ? 'neomorph text-gray-200' 
              : 'text-gray-400'
          ]"
          @click="currentSection = 'basic'"
        >
          <Icon name="lucide:info" class="w-4 h-4 mx-auto mb-1" />
          <div class="text-xs">Basic</div>
        </button>
        <button
          :class="[
            'py-2 px-3 rounded-lg text-sm font-medium transition-all',
            currentSection === 'auth' 
              ? 'neomorph text-gray-200' 
              : 'text-gray-400'
          ]"
          @click="currentSection = 'auth'"
        >
          <Icon name="lucide:key" class="w-4 h-4 mx-auto mb-1" />
          <div class="text-xs">Auth</div>
        </button>
        <button
          :class="[
            'py-2 px-3 rounded-lg text-sm font-medium transition-all',
            currentSection === 'advanced' 
              ? 'neomorph text-gray-200' 
              : 'text-gray-400'
          ]"
          @click="currentSection = 'advanced'"
        >
          <Icon name="lucide:settings" class="w-4 h-4 mx-auto mb-1" />
          <div class="text-xs">Advanced</div>
        </button>
        <button
          :class="[
            'py-2 px-3 rounded-lg text-sm font-medium transition-all',
            currentSection === 'forwarding' 
              ? 'neomorph text-gray-200' 
              : 'text-gray-400'
          ]"
          @click="currentSection = 'forwarding'"
        >
          <Icon name="lucide:arrow-right-left" class="w-4 h-4 mx-auto mb-1" />
          <div class="text-xs">Forward</div>
        </button>
      </div>

      <!-- Basic Section -->
      <div v-show="currentSection === 'basic'" class="space-y-4">
        <!-- Name -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">
            Host Name *
          </label>
          <div class="neomorph-pressed rounded-xl overflow-hidden">
            <input
              v-model="formData.name"
              type="text"
              placeholder="My Server"
              class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
            />
          </div>
        </div>

        <!-- Hostname -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">
            Hostname / IP Address *
          </label>
          <div class="neomorph-pressed rounded-xl overflow-hidden">
            <input
              v-model="formData.hostname"
              type="text"
              placeholder="example.com or 192.168.1.100"
              class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
            />
          </div>
        </div>

        <!-- Port & Username -->
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              Port *
            </label>
            <div class="neomorph-pressed rounded-xl overflow-hidden">
              <input
                v-model.number="formData.port"
                type="number"
                placeholder="22"
                class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
              />
            </div>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              Username *
            </label>
            <div class="neomorph-pressed rounded-xl overflow-hidden">
              <input
                v-model="formData.username"
                type="text"
                placeholder="root"
                class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
              />
            </div>
          </div>
        </div>

        <!-- Description -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">
            Description
          </label>
          <div class="neomorph-pressed rounded-xl overflow-hidden">
            <textarea
              v-model="formData.description"
              placeholder="Optional description..."
              rows="3"
              class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none resize-none"
            />
          </div>
        </div>

        <!-- Tags -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">
            Tags
          </label>
          <div class="flex flex-wrap gap-2 mb-2">
            <span
              v-for="tag in formData.tags"
              :key="tag"
              class="neomorph-pressed px-3 py-1 rounded-lg text-sm text-gray-300 flex items-center gap-2"
            >
              #{{ tag }}
              <button @click="removeTag(tag)">
                <Icon name="lucide:x" class="w-3 h-3" />
              </button>
            </span>
          </div>
          <div class="neomorph-pressed rounded-xl overflow-hidden flex">
            <input
              v-model="tagInput"
              type="text"
              placeholder="Add tag..."
              class="flex-1 px-4 py-3 bg-transparent text-gray-200 outline-none"
              @keyup.enter="addTag"
            />
            <button
              class="px-4 text-blue-400"
              @click="addTag"
            >
              <Icon name="lucide:plus" class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>

      <!-- Auth Section -->
      <div v-show="currentSection === 'auth'" class="space-y-4">
        <!-- Auth Method -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-3">
            Authentication Method
          </label>
          <div class="space-y-2">
            <button
              :class="[
                'w-full p-4 rounded-xl text-left transition-all',
                formData.authMethod === 'privateKey' 
                  ? 'neomorph' 
                  : 'neomorph-pressed'
              ]"
              @click="formData.authMethod = 'privateKey'"
            >
              <div class="flex items-center gap-3">
                <Icon name="lucide:file-key" class="w-6 h-6 text-blue-400" />
                <div>
                  <div class="font-medium text-gray-200">Private Key</div>
                  <div class="text-xs text-gray-400">Use SSH key file (Recommended)</div>
                </div>
              </div>
            </button>

            <button
              :class="[
                'w-full p-4 rounded-xl text-left transition-all',
                formData.authMethod === 'password' 
                  ? 'neomorph' 
                  : 'neomorph-pressed'
              ]"
              @click="formData.authMethod = 'password'"
            >
              <div class="flex items-center gap-3">
                <Icon name="lucide:key" class="w-6 h-6 text-yellow-400" />
                <div>
                  <div class="font-medium text-gray-200">Password</div>
                  <div class="text-xs text-gray-400">Use password authentication</div>
                </div>
              </div>
            </button>

            <button
              :class="[
                'w-full p-4 rounded-xl text-left transition-all',
                formData.authMethod === 'agent' 
                  ? 'neomorph' 
                  : 'neomorph-pressed'
              ]"
              @click="formData.authMethod = 'agent'"
            >
              <div class="flex items-center gap-3">
                <Icon name="lucide:shield" class="w-6 h-6 text-green-400" />
                <div>
                  <div class="font-medium text-gray-200">SSH Agent</div>
                  <div class="text-xs text-gray-400">Use SSH agent forwarding</div>
                </div>
              </div>
            </button>
          </div>
        </div>

        <!-- Password Fields -->
        <div v-if="formData.authMethod === 'password'" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              Password
            </label>
            <div class="neomorph-pressed rounded-xl overflow-hidden">
              <input
                v-model="formData.password"
                type="password"
                placeholder="Enter password"
                class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
              />
            </div>
            <p class="text-xs text-yellow-400 mt-2 flex items-center gap-1">
              <Icon name="lucide:alert-triangle" class="w-3 h-3" />
              Passwords are stored locally
            </p>
          </div>
        </div>

        <!-- Private Key Fields -->
        <div v-if="formData.authMethod === 'privateKey'" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              Private Key Path
            </label>
            <div class="neomorph-pressed rounded-xl overflow-hidden">
              <input
                v-model="formData.privateKeyPath"
                type="text"
                placeholder="~/.ssh/id_rsa"
                class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              Or Paste Private Key
            </label>
            <div class="neomorph-pressed rounded-xl overflow-hidden">
              <textarea
                v-model="formData.privateKey"
                placeholder="-----BEGIN OPENSSH PRIVATE KEY-----"
                rows="4"
                class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none resize-none font-mono text-xs"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">
              Passphrase (if protected)
            </label>
            <div class="neomorph-pressed rounded-xl overflow-hidden">
              <input
                v-model="formData.passphrase"
                type="password"
                placeholder="Enter passphrase"
                class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Advanced Section -->
      <div v-show="currentSection === 'advanced'" class="space-y-4">
        <!-- Toggles -->
        <div class="space-y-3">
          <div class="neomorph p-4 rounded-xl flex items-center justify-between">
            <div>
              <div class="font-medium text-gray-200">Forward Agent</div>
              <div class="text-xs text-gray-400">Allow agent forwarding</div>
            </div>
            <button
              :class="[
                'w-14 h-8 rounded-full transition-all',
                formData.forwardAgent ? 'bg-blue-500' : 'neomorph-pressed'
              ]"
              @click="formData.forwardAgent = !formData.forwardAgent"
            >
              <div
                :class="[
                  'w-6 h-6 rounded-full bg-white transition-transform',
                  formData.forwardAgent ? 'translate-x-7' : 'translate-x-1'
                ]"
              />
            </button>
          </div>

          <div class="neomorph p-4 rounded-xl flex items-center justify-between">
            <div>
              <div class="font-medium text-gray-200">Compression</div>
              <div class="text-xs text-gray-400">Enable data compression</div>
            </div>
            <button
              :class="[
                'w-14 h-8 rounded-full transition-all',
                formData.compression ? 'bg-blue-500' : 'neomorph-pressed'
              ]"
              @click="formData.compression = !formData.compression"
            >
              <div
                :class="[
                  'w-6 h-6 rounded-full bg-white transition-transform',
                  formData.compression ? 'translate-x-7' : 'translate-x-1'
                ]"
              />
            </button>
          </div>

          <div class="neomorph p-4 rounded-xl flex items-center justify-between">
            <div>
              <div class="font-medium text-gray-200">Keep Alive</div>
              <div class="text-xs text-gray-400">Send keep-alive packets</div>
            </div>
            <button
              :class="[
                'w-14 h-8 rounded-full transition-all',
                formData.keepAlive ? 'bg-blue-500' : 'neomorph-pressed'
              ]"
              @click="formData.keepAlive = !formData.keepAlive"
            >
              <div
                :class="[
                  'w-6 h-6 rounded-full bg-white transition-transform',
                  formData.keepAlive ? 'translate-x-7' : 'translate-x-1'
                ]"
              />
            </button>
          </div>

          <div class="neomorph p-4 rounded-xl flex items-center justify-between">
            <div>
              <div class="font-medium text-gray-200">Strict Host Key Checking</div>
              <div class="text-xs text-gray-400">Verify host keys</div>
            </div>
            <button
              :class="[
                'w-14 h-8 rounded-full transition-all',
                formData.strictHostKeyChecking ? 'bg-blue-500' : 'neomorph-pressed'
              ]"
              @click="formData.strictHostKeyChecking = !formData.strictHostKeyChecking"
            >
              <div
                :class="[
                  'w-6 h-6 rounded-full bg-white transition-transform',
                  formData.strictHostKeyChecking ? 'translate-x-7' : 'translate-x-1'
                ]"
              />
            </button>
          </div>
        </div>

        <!-- Proxy Jump -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">
            Proxy Jump
          </label>
          <div class="neomorph-pressed rounded-xl overflow-hidden">
            <input
              v-model="formData.proxyJump"
              type="text"
              placeholder="user@jumphost"
              class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
            />
          </div>
          <p class="text-xs text-gray-400 mt-2">
            Jump through an intermediate host
          </p>
        </div>
      </div>

      <!-- Forwarding Section -->
      <div v-show="currentSection === 'forwarding'" class="space-y-6">
        <!-- Local Forwards -->
        <div>
          <div class="flex items-center justify-between mb-3">
            <label class="text-sm font-medium text-gray-300">
              Local Port Forwarding
            </label>
            <button
              class="neomorph-btn px-3 py-1 rounded-lg text-sm text-gray-200"
              @click="addLocalForward"
            >
              <Icon name="lucide:plus" class="w-4 h-4" />
            </button>
          </div>
          
          <div v-if="formData.localForwards.length === 0" class="neomorph-pressed p-4 rounded-xl text-center text-gray-500 text-sm">
            No local forwards configured
          </div>
          
          <div v-else class="space-y-3">
            <div
              v-for="(fwd, index) in formData.localForwards"
              :key="index"
              class="neomorph p-4 rounded-xl"
            >
              <div class="flex items-center justify-between mb-3">
                <span class="text-sm font-medium text-gray-300">Forward #{{ index + 1 }}</span>
                <button
                  class="text-red-400"
                  @click="removeLocalForward(index)"
                >
                  <Icon name="lucide:trash-2" class="w-4 h-4" />
                </button>
              </div>
              <div class="grid grid-cols-3 gap-2">
                <div>
                  <label class="text-xs text-gray-400 mb-1 block">Local Port</label>
                  <input
                    v-model.number="fwd.localPort"
                    type="number"
                    class="w-full px-3 py-2 neomorph-pressed rounded-lg bg-transparent text-gray-200 outline-none text-sm"
                  />
                </div>
                <div>
                  <label class="text-xs text-gray-400 mb-1 block">Remote Host</label>
                  <input
                    v-model="fwd.remoteHost"
                    type="text"
                    class="w-full px-3 py-2 neomorph-pressed rounded-lg bg-transparent text-gray-200 outline-none text-sm"
                  />
                </div>
                <div>
                  <label class="text-xs text-gray-400 mb-1 block">Remote Port</label>
                  <input
                    v-model.number="fwd.remotePort"
                    type="number"
                    class="w-full px-3 py-2 neomorph-pressed rounded-lg bg-transparent text-gray-200 outline-none text-sm"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Remote Forwards -->
        <div>
          <div class="flex items-center justify-between mb-3">
            <label class="text-sm font-medium text-gray-300">
              Remote Port Forwarding
            </label>
            <button
              class="neomorph-btn px-3 py-1 rounded-lg text-sm text-gray-200"
              @click="addRemoteForward"
            >
              <Icon name="lucide:plus" class="w-4 h-4" />
            </button>
          </div>
          
          <div v-if="formData.remoteForwards.length === 0" class="neomorph-pressed p-4 rounded-xl text-center text-gray-500 text-sm">
            No remote forwards configured
          </div>
          
          <div v-else class="space-y-3">
            <div
              v-for="(fwd, index) in formData.remoteForwards"
              :key="index"
              class="neomorph p-4 rounded-xl"
            >
              <div class="flex items-center justify-between mb-3">
                <span class="text-sm font-medium text-gray-300">Forward #{{ index + 1 }}</span>
                <button
                  class="text-red-400"
                  @click="removeRemoteForward(index)"
                >
                  <Icon name="lucide:trash-2" class="w-4 h-4" />
                </button>
              </div>
              <div class="grid grid-cols-3 gap-2">
                <div>
                  <label class="text-xs text-gray-400 mb-1 block">Remote Port</label>
                  <input
                    v-model.number="fwd.remotePort"
                    type="number"
                    class="w-full px-3 py-2 neomorph-pressed rounded-lg bg-transparent text-gray-200 outline-none text-sm"
                  />
                </div>
                <div>
                  <label class="text-xs text-gray-400 mb-1 block">Local Host</label>
                  <input
                    v-model="fwd.localHost"
                    type="text"
                    class="w-full px-3 py-2 neomorph-pressed rounded-lg bg-transparent text-gray-200 outline-none text-sm"
                  />
                </div>
                <div>
                  <label class="text-xs text-gray-400 mb-1 block">Local Port</label>
                  <input
                    v-model.number="fwd.localPort"
                    type="number"
                    class="w-full px-3 py-2 neomorph-pressed rounded-lg bg-transparent text-gray-200 outline-none text-sm"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Fixed Bottom Actions -->
    <div class="fixed bottom-0 left-0 right-0 p-4 neomorph border-t border-gray-700">
      <div class="flex gap-3 max-w-2xl mx-auto">
        <button
          class="flex-1 neomorph-pressed py-3 rounded-xl font-medium text-gray-300"
          @click="handleCancel"
        >
          Cancel
        </button>
        <button
          :disabled="!isValid"
          :class="[
            'flex-2 py-3 px-6 rounded-xl font-medium text-white flex items-center justify-center gap-2',
            isValid
              ? 'neomorph-btn'
              : 'neomorph-pressed opacity-50 cursor-not-allowed'
          ]"
          @click="handleSave"
        >
          <Icon name="lucide:save" class="w-5 h-5" />
          Update Host
        </button>
      </div>
    </div>
  </Page>
</template>

