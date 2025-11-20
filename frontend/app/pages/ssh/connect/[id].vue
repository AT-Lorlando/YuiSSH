<script setup lang="ts">
import { useSSHStore } from '~/stores/ssh'
import XtermTerminal from '~/components/XtermTerminal.vue'
import { themes } from '~/utils/themes'

definePageMeta({
  layout: 'blank'
})

const router = useRouter()
const route = useRoute()
const sshStore = useSSHStore()
const { $ssh } = useNuxtApp()

const hostId = route.params.id as string
const host = ref(null as any)
const connectionStatus = ref<'idle' | 'connecting' | 'connected' | 'error'>('idle')
const terminalRef = ref<InstanceType<typeof XtermTerminal> | null>(null)

// Terminal Settings
const showSettings = ref(false)
const currentTheme = ref('default')
const fontSize = ref(12)
const isCtrlActive = ref(false)

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

// Cleanup on unmount
onUnmounted(() => {
  ssh.cleanupShellSession()
})

// Connect to SSH
const handleConnect = async () => {
  if (!host.value) return
  
  connectionStatus.value = 'connecting'
  terminalRef.value?.clear()
  terminalRef.value?.write('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\r\n')
  terminalRef.value?.write(`🔌 Initiating SSH connection...\r\n`)
  terminalRef.value?.write(`   Host: ${host.value.hostname}:${host.value.port}\r\n`)
  terminalRef.value?.write(`   User: ${host.value.username}\r\n`)
  terminalRef.value?.write('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\r\n\r\n')
  
  try {
    let result
    
    // Use secure key authentication if available
    if (host.value.authMethod === 'secureKey' && host.value.secureKeyId) {
      terminalRef.value?.write(`🔐 Using secure key: ${host.value.secureKeyLabel}\r\n`)
      terminalRef.value?.write('👆 Biometric authentication required...\r\n')
      
      result = await $ssh.connectWithSecureKey({
        hostname: host.value.hostname,
        port: host.value.port,
        username: host.value.username,
        keyId: host.value.secureKeyId
      })
      
      if (result.success && result.sessionId) {
        handleSuccessfulConnection(result.sessionId)
        return
      }
    } else if (host.value.authMethod === 'password') {
      terminalRef.value?.write('🔑 Using password authentication...\r\n')
      
      const options = {
        hostname: host.value.hostname,
        port: host.value.port,
        username: host.value.username,
        authMethod: host.value.authMethod,
        password: host.value.password,
        privateKey: host.value.privateKey,
        privateKeyPath: host.value.privateKeyPath,
        passphrase: host.value.passphrase,
      }
      
      const success = await ssh.connect(options)
      if (success && ssh.currentSessionId.value) {
        handleSuccessfulConnection(ssh.currentSessionId.value)
        return
      }
    } else {
      // Agent or other auth methods
      terminalRef.value?.write(`🔐 Using ${host.value.authMethod} authentication...\r\n`)
      
      const options = {
        hostname: host.value.hostname,
        port: host.value.port,
        username: host.value.username,
        authMethod: host.value.authMethod,
        password: host.value.password,
        privateKey: host.value.privateKey,
        privateKeyPath: host.value.privateKeyPath,
        passphrase: host.value.passphrase,
      }
      
      const success = await ssh.connect(options)
      if (success && ssh.currentSessionId.value) {
        handleSuccessfulConnection(ssh.currentSessionId.value)
        return
      }
    }
    
    // If we get here, connection failed
    throw new Error(ssh.connectionError.value || 'Connection failed')
    
  } catch (error: any) {
    connectionStatus.value = 'error'
    terminalRef.value?.write('\r\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\r\n')
    terminalRef.value?.write(`✗ Connection error: ${error.message}\r\n`)
    terminalRef.value?.write('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\r\n')
  }
}

const handleSuccessfulConnection = async (sessionId: string) => {
  ssh.currentSessionId.value = sessionId
  ssh.isConnected.value = true
  
  terminalRef.value?.write('✓ Connected successfully\r\n')
  terminalRef.value?.write('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\r\n\r\n')
  
  // Start interactive shell session
  const shellStarted = await ssh.startShellSession((output) => {
    terminalRef.value?.write(output)
  })
  
  if (shellStarted) {
    connectionStatus.value = 'connected'
    terminalRef.value?.clear() // Clear init logs for clean shell
    terminalRef.value?.focus()
    
    // Force resize to ensure fit
    setTimeout(() => {
      terminalRef.value?.handleResize()
    }, 100)
  } else {
    throw new Error('Failed to start shell session')
  }
}

const handleDisconnect = async () => {
  terminalRef.value?.write('\r\nDisconnecting...\r\n')
  ssh.cleanupShellSession()
  await ssh.disconnect()
  connectionStatus.value = 'idle'
  terminalRef.value?.write('Disconnected\r\n')
}

const onTerminalData = async (data: string) => {
  if (connectionStatus.value === 'connected') {
    let dataToSend = data
    
    // Handle Ctrl key modifier
    if (isCtrlActive.value) {
      // If data is a single character, convert to control code
      if (data.length === 1) {
        const charCode = data.toUpperCase().charCodeAt(0)
        if (charCode >= 64 && charCode <= 95) {
          dataToSend = String.fromCharCode(charCode - 64)
        }
      }
      isCtrlActive.value = false // Reset Ctrl after one use
    }
    
    await ssh.sendToShell(dataToSend)
  }
}

// Quick commands
const quickCommand = async (cmd: string) => {
  if (connectionStatus.value === 'connected') {
    await ssh.sendToShell(cmd + '\n')
    terminalRef.value?.focus()
  }
}

const toggleCtrl = () => {
  isCtrlActive.value = !isCtrlActive.value
}

const goBack = () => {
  if (connectionStatus.value === 'connected') {
    handleDisconnect()
  }
  router.back()
}

// Virtual Keyboard helper
const showKeyboard = () => {
  terminalRef.value?.focus()
}
</script>

<template>
    <div v-if="host" class="h-screen w-screen flex flex-col bg-black overflow-hidden">
      <!-- Header -->
      <div class="px-4 py-3 border-b border-slate-800 bg-slate-900/95 backdrop-blur flex items-center justify-between shrink-0 z-50">
        <div class="flex items-center gap-3">
          <button
            class="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors"
            tabindex="-1"
            @mousedown.prevent
            @click="goBack"
          >
            <Icon name="lucide:arrow-left" class="w-5 h-5 text-slate-300" />
          </button>
          <div>
            <h1 class="text-base font-bold text-slate-200">{{ host.name }}</h1>
            <p class="text-xs text-slate-400">{{ host.username }}@{{ host.hostname }}</p>
          </div>
        </div>
        
        <!-- Header Actions -->
        <div class="flex items-center gap-2">
           <!-- Settings Button -->
           <button
            class="p-2 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors"
            tabindex="-1"
            @mousedown.prevent
            @click="showSettings = true"
          >
            <Icon name="lucide:settings-2" class="w-5 h-5 text-slate-300" />
          </button>
          
           <div
            :class="[
              'w-2 h-2 rounded-full',
              connectionStatus === 'connected' ? 'bg-green-400 animate-pulse' :
              connectionStatus === 'connecting' ? 'bg-blue-400 animate-pulse' :
              connectionStatus === 'error' ? 'bg-red-400' :
              'bg-slate-400'
            ]"
          />
        </div>
      </div>

      <!-- Connection Controls (Overlay) -->
      <div v-if="connectionStatus === 'idle' || connectionStatus === 'error'" class="absolute inset-0 z-40 bg-slate-900/90 backdrop-blur-sm flex items-center justify-center p-4">
        <div class="card max-w-sm w-full p-6 text-center bg-slate-800 border border-slate-700 shadow-2xl">
          <div class="w-16 h-16 rounded-full mx-auto mb-4 flex items-center justify-center"
               :class="connectionStatus === 'error' ? 'bg-red-500/10' : 'bg-blue-500/10'">
            <Icon
              :name="connectionStatus === 'error' ? 'lucide:x-circle' : 'lucide:power'"
              class="w-8 h-8"
              :class="connectionStatus === 'error' ? 'text-red-400' : 'text-blue-400'"
            />
          </div>
          
          <h3 class="text-lg font-bold text-slate-200 mb-2">
            {{ connectionStatus === 'error' ? 'Connection Failed' : 'Ready to Connect' }}
          </h3>
          
          <p v-if="connectionStatus === 'error' && ssh.connectionError.value" class="text-red-400 text-sm mb-6">
            {{ ssh.connectionError.value }}
          </p>
          <p v-else class="text-slate-400 text-sm mb-6">
            Establish SSH connection to {{ host.hostname }}
          </p>
          
          <button
            class="btn-primary w-full justify-center"
            tabindex="-1"
            @mousedown.prevent
            @click="handleConnect"
          >
            <Icon :name="connectionStatus === 'error' ? 'lucide:refresh-cw' : 'lucide:plug'" class="w-4 h-4" />
            {{ connectionStatus === 'error' ? 'Retry' : 'Connect' }}
          </button>
        </div>
      </div>

      <!-- Terminal Area -->
      <div class="flex-1 relative overflow-hidden bg-black">
        <XtermTerminal
          ref="terminalRef"
          :theme="themes[currentTheme]"
          :font-size="fontSize"
          @data="onTerminalData"
          class="absolute inset-0"
        />
      </div>

      <!-- Mobile Quick Actions Bar -->
      <div v-if="connectionStatus === 'connected'" class="shrink-0 border-t border-slate-800 bg-slate-900 p-2 pb-safe">
        <div class="flex items-center gap-2 overflow-x-auto no-scrollbar">
          <button
            class="px-3 py-2 rounded bg-slate-800 active:bg-slate-700 text-xs text-slate-300 font-mono whitespace-nowrap border border-slate-700"
            tabindex="-1"
            @mousedown.prevent
            @click="showKeyboard"
          >
            <Icon name="lucide:keyboard" class="w-4 h-4" />
          </button>
          
          <div class="w-px h-6 bg-slate-700 mx-1"></div>
          
          <!-- Ctrl Key Toggle -->
          <button
            class="px-3 py-2 rounded text-xs font-mono whitespace-nowrap border transition-colors"
            :class="isCtrlActive 
              ? 'bg-blue-600 text-white border-blue-500' 
              : 'bg-slate-800 text-slate-300 border-slate-700 active:bg-slate-700'"
            tabindex="-1"
            @mousedown.prevent
            @click="toggleCtrl"
          >
            CTRL
          </button>
          
          <button
            v-for="cmd in ['TAB', 'ESC', '/', '-', '|', 'HOME', 'END']"
            :key="cmd"
            class="px-3 py-2 rounded bg-slate-800 active:bg-slate-700 text-xs text-slate-300 font-mono whitespace-nowrap border border-slate-700"
            tabindex="-1"
            @mousedown.prevent
            @click="async () => {
              if (cmd === 'TAB') await ssh.sendToShell('\t')
              else if (cmd === 'ESC') await ssh.sendToShell('\x1b')
              else if (cmd === 'HOME') await ssh.sendToShell('\x1b[H')
              else if (cmd === 'END') await ssh.sendToShell('\x1b[F')
              else await ssh.sendToShell(cmd)
              terminalRef?.focus()
            }"
          >
            {{ cmd }}
          </button>
        </div>
      </div>

      <!-- Settings Modal -->
      <div v-if="showSettings" class="fixed inset-0 z-[100] flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-sm" @click="showSettings = false">
        <div class="w-full max-w-md bg-slate-900 border-t sm:border border-slate-800 sm:rounded-xl p-6 pb-safe" @click.stop>
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold text-slate-200">Terminal Settings</h3>
            <button @click="showSettings = false" class="p-2 hover:bg-slate-800 rounded-lg">
              <Icon name="lucide:x" class="w-5 h-5 text-slate-400" />
            </button>
          </div>

          <div class="space-y-6">
            <!-- Theme Selector -->
            <div>
              <label class="block text-sm font-medium text-slate-400 mb-3">Theme</label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  v-for="(theme, key) in themes"
                  :key="key"
                  class="p-3 rounded-lg border transition-all text-left flex items-center gap-3"
                  :class="currentTheme === key ? 'bg-slate-800 border-blue-500 ring-1 ring-blue-500' : 'bg-slate-800/50 border-slate-700 hover:border-slate-600'"
                  @click="currentTheme = key as string"
                >
                  <div class="w-6 h-6 rounded-full border border-slate-600" :style="{ background: theme.background }"></div>
                  <span class="text-sm text-slate-200">{{ theme.name }}</span>
                </button>
              </div>
            </div>

            <!-- Font Size -->
            <div>
              <label class="block text-sm font-medium text-slate-400 mb-3">
                Font Size: <span class="text-slate-200">{{ fontSize }}px</span>
              </label>
              <input
                v-model.number="fontSize"
                type="range"
                min="8"
                max="20"
                step="1"
                class="w-full accent-blue-500 h-2 bg-slate-800 rounded-lg appearance-none cursor-pointer"
              />
              <div class="flex justify-between text-xs text-slate-500 mt-2">
                <span>Tiny (8px)</span>
                <span>Huge (20px)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<style scoped>
.pb-safe {
  padding-bottom: env(safe-area-inset-bottom, 20px);
}
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
