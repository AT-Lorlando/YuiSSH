<script setup lang="ts">
import { Page } from 'konsta/vue'
import { useSSHStore } from '~/stores/ssh'

const router = useRouter()
const route = useRoute()
const sshStore = useSSHStore()

const hostId = route.params.id as string
const host = ref(null as any)
const connectionStatus = ref<'idle' | 'connecting' | 'connected' | 'error'>('idle')
const terminal = ref<HTMLElement | null>(null)
const commandInput = ref('')
const terminalOutput = ref<string[]>([])

// Use SSH composable
const ssh = useSSH()

// Load host on mount
onMounted(() => {
  sshStore.loadHosts()
  const foundHost = sshStore.getHostById(hostId)
  
  if (!foundHost) {
    router.push('/ssh')
    return
  }
  
  host.value = foundHost
  sshStore.markHostAsUsed(hostId)
})

// Connect to SSH
const handleConnect = async () => {
  if (!host.value) return
  
  connectionStatus.value = 'connecting'
  addToTerminal(`Connecting to ${host.value.username}@${host.value.hostname}:${host.value.port}...`)
  
  try {
    const options = {
      hostname: host.value.hostname,
      port: host.value.port,
      username: host.value.username,
      authMethod: host.value.authMethod,
      password: host.value.password,
      privateKey: host.value.privateKey,
      passphrase: host.value.passphrase,
    }
    
    const success = await ssh.connect(options)
    
    if (success) {
      connectionStatus.value = 'connected'
      addToTerminal('✓ Connected successfully!')
      addToTerminal(`Welcome to ${host.value.name}`)
      addToTerminal('')
    } else {
      connectionStatus.value = 'error'
      addToTerminal(`✗ Connection failed: ${ssh.connectionError.value}`)
    }
    
  } catch (error: any) {
    connectionStatus.value = 'error'
    addToTerminal(`✗ Error: ${error.message || 'Connection failed'}`)
  }
}

const handleDisconnect = async () => {
  addToTerminal('Disconnecting...')
  await ssh.disconnect()
  connectionStatus.value = 'idle'
  addToTerminal('Disconnected')
  
  // Clear terminal after a delay
  setTimeout(() => {
    terminalOutput.value = []
  }, 1000)
}

const addToTerminal = (text: string) => {
  terminalOutput.value.push(text)
  nextTick(() => {
    if (terminal.value) {
      terminal.value.scrollTop = terminal.value.scrollHeight
    }
  })
}

const sendCommand = async () => {
  if (!commandInput.value.trim() || connectionStatus.value !== 'connected') return
  
  const cmd = commandInput.value
  addToTerminal(`$ ${cmd}`)
  commandInput.value = ''
  
  try {
    const output = await ssh.executeCommand(cmd)
    if (output) {
      output.split('\n').forEach(line => addToTerminal(line))
    }
    addToTerminal('')
  } catch (error: any) {
    addToTerminal(`Error: ${error.message}`)
    addToTerminal('')
  }
}

const goBack = () => {
  if (connectionStatus.value === 'connected') {
    handleDisconnect()
  }
  router.back()
}
</script>

<template>
  <Page>
    <Breadcrumbs />
    
    <div v-if="host" class="min-h-screen flex flex-col">
      <!-- Header -->
      <div class="px-4 pt-6 pb-4">
        <div class="flex items-center gap-3 mb-2">
          <button
            class="neomorph-btn p-2 rounded-lg"
            @click="goBack"
          >
            <Icon name="lucide:arrow-left" class="w-5 h-5 text-gray-300" />
          </button>
          <div class="flex-1">
            <h1 class="text-2xl font-bold text-gray-100">{{ host.name }}</h1>
            <p class="text-sm text-gray-400">{{ host.username }}@{{ host.hostname }}:{{ host.port }}</p>
          </div>
          
          <!-- Connection Status Badge -->
          <div
            :class="[
              'px-3 py-1 rounded-lg text-xs font-medium flex items-center gap-2',
              connectionStatus === 'connected' ? 'bg-green-500/20 text-green-400' :
              connectionStatus === 'connecting' ? 'bg-blue-500/20 text-blue-400' :
              connectionStatus === 'error' ? 'bg-red-500/20 text-red-400' :
              'bg-gray-500/20 text-gray-400'
            ]"
          >
            <div
              :class="[
                'w-2 h-2 rounded-full',
                connectionStatus === 'connected' ? 'bg-green-400 animate-pulse' :
                connectionStatus === 'connecting' ? 'bg-blue-400 animate-pulse' :
                connectionStatus === 'error' ? 'bg-red-400' :
                'bg-gray-400'
              ]"
            />
            {{ 
              connectionStatus === 'connected' ? 'Connected' :
              connectionStatus === 'connecting' ? 'Connecting...' :
              connectionStatus === 'error' ? 'Error' :
              'Disconnected'
            }}
          </div>
        </div>
      </div>

      <!-- Connection Controls -->
      <div v-if="connectionStatus === 'idle' || connectionStatus === 'error'" class="px-4 pb-4">
        <div class="neomorph p-6 rounded-2xl text-center">
          <div class="neomorph-pressed w-20 h-20 rounded-full mx-auto mb-4 flex items-center justify-center">
            <Icon
              :name="connectionStatus === 'error' ? 'lucide:x-circle' : 'lucide:power'"
              :class="[
                'w-10 h-10',
                connectionStatus === 'error' ? 'text-red-400' : 'text-blue-400'
              ]"
            />
          </div>
          
          <h3 class="text-xl font-bold text-gray-100 mb-2">
            {{ connectionStatus === 'error' ? 'Connection Failed' : 'Ready to Connect' }}
          </h3>
          
          <p v-if="connectionError" class="text-red-400 text-sm mb-4">
            {{ connectionError }}
          </p>
          
          <p v-else class="text-gray-400 text-sm mb-6">
            Click connect to establish SSH connection
          </p>
          
          <button
            class="neomorph-btn px-8 py-3 rounded-xl font-medium text-gray-200 inline-flex items-center gap-2"
            @click="handleConnect"
          >
            <Icon name="lucide:plug" class="w-5 h-5" />
            {{ connectionStatus === 'error' ? 'Retry Connection' : 'Connect' }}
          </button>
        </div>

        <!-- Host Info -->
        <div class="mt-4 neomorph p-4 rounded-xl">
          <h4 class="text-sm font-semibold text-gray-300 mb-3">Connection Details</h4>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-400">Hostname:</span>
              <span class="text-gray-200 font-mono">{{ host.hostname }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-400">Port:</span>
              <span class="text-gray-200 font-mono">{{ host.port }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-400">Username:</span>
              <span class="text-gray-200 font-mono">{{ host.username }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-400">Auth Method:</span>
              <span class="text-gray-200 capitalize">{{ host.authMethod }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Terminal (when connected) -->
      <div v-if="connectionStatus === 'connecting' || connectionStatus === 'connected'" class="flex-1 flex flex-col px-4 pb-4">
        <!-- Terminal Output -->
        <div class="flex-1 neomorph-pressed rounded-t-2xl overflow-hidden">
          <div
            ref="terminal"
            class="h-full overflow-y-auto p-4 font-mono text-sm text-green-400"
          >
            <div v-if="connectionStatus === 'connecting'" class="flex items-center gap-3">
              <div class="animate-spin">
                <Icon name="lucide:loader-2" class="w-5 h-5" />
              </div>
              <span>Connecting to {{ host.hostname }}...</span>
            </div>
            
            <div v-for="(line, index) in terminalOutput" :key="index" class="mb-1">
              {{ line }}
            </div>
            
            <div v-if="connectionStatus === 'connected'" class="flex items-center gap-2 mt-2">
              <span class="text-blue-400">$</span>
              <div class="w-2 h-4 bg-green-400 animate-pulse" />
            </div>
          </div>
        </div>

        <!-- Command Input -->
        <div v-if="connectionStatus === 'connected'" class="neomorph-pressed rounded-b-2xl p-4 flex items-center gap-3">
          <Icon name="lucide:terminal" class="w-5 h-5 text-gray-400" />
          <input
            v-model="commandInput"
            type="text"
            placeholder="Enter command..."
            class="flex-1 bg-transparent text-gray-200 outline-none font-mono"
            @keyup.enter="sendCommand"
          />
          <button
            class="neomorph-btn p-2 rounded-lg"
            @click="sendCommand"
          >
            <Icon name="lucide:send" class="w-5 h-5 text-gray-300" />
          </button>
        </div>

        <!-- Disconnect Button -->
        <button
          v-if="connectionStatus === 'connected'"
          class="mt-4 w-full py-3 rounded-xl font-medium text-white flex items-center justify-center gap-2"
          style="background: linear-gradient(145deg, #dc2626, #b91c1c); box-shadow: 6px 6px 12px rgba(0, 0, 0, 0.4), -6px -6px 12px rgba(239, 68, 68, 0.1);"
          @click="handleDisconnect"
        >
          <Icon name="lucide:power-off" class="w-5 h-5" />
          Disconnect
        </button>
      </div>

      <!-- Quick Actions (when connected) -->
      <div v-if="connectionStatus === 'connected'" class="px-4 pb-6">
        <div class="grid grid-cols-3 gap-3">
          <button class="neomorph p-3 rounded-xl text-center">
            <Icon name="lucide:folder" class="w-6 h-6 mx-auto mb-1 text-blue-400" />
            <span class="text-xs text-gray-300">Files</span>
          </button>
          <button class="neomorph p-3 rounded-xl text-center">
            <Icon name="lucide:upload" class="w-6 h-6 mx-auto mb-1 text-green-400" />
            <span class="text-xs text-gray-300">Upload</span>
          </button>
          <button class="neomorph p-3 rounded-xl text-center">
            <Icon name="lucide:settings" class="w-6 h-6 mx-auto mb-1 text-gray-400" />
            <span class="text-xs text-gray-300">Settings</span>
          </button>
        </div>
      </div>
    </div>
  </Page>
</template>

