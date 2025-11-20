import { registerPlugin } from '@capacitor/core'

export interface SSHConnectionOptions {
  hostname: string
  port: number
  username: string
  authMethod: 'password' | 'privateKey' | 'agent'
  password?: string
  privateKey?: string
  passphrase?: string
}

export interface SSHConnectionResult {
  success: boolean
  message?: string
  sessionId?: string
}

export interface SSHCommandOptions {
  sessionId: string
  command: string
}

export interface SSHCommandResult {
  success: boolean
  output?: string
  error?: string
}

export interface SSHPlugin {
  connect(options: SSHConnectionOptions): Promise<SSHConnectionResult>
  disconnect(options: { sessionId: string }): Promise<{ success: boolean }>
  executeCommand(options: SSHCommandOptions): Promise<SSHCommandResult>
  isConnected(options: { sessionId: string }): Promise<{ connected: boolean }>

  // Legacy key generation (insecure - exposes private key)
  generateKeyPair(options: { keyType?: string; keySize?: number; comment?: string }): Promise<{
    success: boolean
    privateKey: string
    publicKey: string
    keyType: string
    keySize: number
  }>

  // Secure key management (hardware-backed)
  generateSecureKeyPair(options: {
    keyType?: string
    keySize?: number
    label?: string
  }): Promise<{
    success: boolean
    keyId: string
    publicKey: string
    alias: string
    label: string
  }>

  listStoredKeys(): Promise<{
    keys: Array<{
      keyId: string
      label: string
      publicKey: string
      keyType: string
      createdAt: string
    }>
  }>

  deleteSecureKey(options: { keyId: string }): Promise<{ success: boolean }>

  connectWithSecureKey(options: {
    hostname: string
    port: number
    username: string
    keyId: string
  }): Promise<SSHConnectionResult>

  // Interactive shell session
  startShellSession(options: { sessionId: string }): Promise<{ success: boolean }>
  sendToShell(options: { sessionId: string; command: string }): Promise<{ success: boolean }>

  // Event listener for shell output
  addListener(
    eventName: 'shellOutput',
    listenerFunc: (data: { sessionId: string; output: string }) => void
  ): Promise<{ remove: () => void }>
}

// Register the plugin
const SSHClient = registerPlugin<SSHPlugin>('SSHClient', {
  web: async () => {
    // Web implementation (mock for development)
    return {
      async connect(options: SSHConnectionOptions): Promise<SSHConnectionResult> {
        console.log('SSH Connect (Web Mock):', options)
        await new Promise(resolve => setTimeout(resolve, 1000))
        return {
          success: true,
          sessionId: `session_${Date.now()}`,
          message: 'Connected (Web Mock)'
        }
      },
      async disconnect(options: { sessionId: string }): Promise<{ success: boolean }> {
        console.log('SSH Disconnect (Web Mock):', options)
        await new Promise(resolve => setTimeout(resolve, 500))
        return { success: true }
      },
      async executeCommand(options: SSHCommandOptions): Promise<SSHCommandResult> {
        console.log('SSH Execute Command (Web Mock):', options)
        await new Promise(resolve => setTimeout(resolve, 800))
        return {
          success: true,
          output: `Mock output for: ${options.command}\nThis is simulated output.`
        }
      },
      async isConnected(options: { sessionId: string }): Promise<{ connected: boolean }> {
        return { connected: true }
      },
      async generateKeyPair(options: { keyType?: string; keySize?: number; comment?: string }) {
        console.log('SSH GenerateKeyPair (Web Mock):', options)
        await new Promise(resolve => setTimeout(resolve, 1000))
        return {
          success: true,
          privateKey: '-----BEGIN OPENSSH PRIVATE KEY-----\nMockPrivateKey\n-----END OPENSSH PRIVATE KEY-----',
          publicKey: 'ssh-rsa MockPublicKey',
          keyType: options.keyType || 'rsa',
          keySize: options.keySize || 2048
        }
      },
      async generateSecureKeyPair(options: { keyType?: string; keySize?: number; label?: string }) {
        console.log('SSH GenerateSecureKeyPair (Web Mock):', options)
        await new Promise(resolve => setTimeout(resolve, 1000))
        return {
          success: true,
          keyId: 'mock-key-id',
          publicKey: 'ssh-rsa MockSecurePublicKey',
          alias: 'mock-alias',
          label: options.label || 'Mock Key'
        }
      },
      async listStoredKeys() {
        console.log('SSH ListStoredKeys (Web Mock)')
        return {
          keys: []
        }
      },
      async deleteSecureKey(options: { keyId: string }) {
        console.log('SSH DeleteSecureKey (Web Mock):', options)
        return { success: true }
      },
      async connectWithSecureKey(options: any) {
        console.log('SSH ConnectWithSecureKey (Web Mock):', options)
        await new Promise(resolve => setTimeout(resolve, 1000))
        return {
          success: true,
          sessionId: 'mock-session-id',
          message: 'Connected with mock secure key'
        }
      },
      async startShellSession(options: { sessionId: string }) {
        console.log('SSH StartShellSession (Web Mock):', options)
        await new Promise(resolve => setTimeout(resolve, 500))
        return { success: true }
      },
      async sendToShell(options: { sessionId: string; command: string }) {
        console.log('SSH SendToShell (Web Mock):', options)
        // Simulate command output after a delay
        setTimeout(() => {
          const mockOutput = `Mock output for: ${options.command}\n$ `
          // Trigger listener if registered
          if ((this as any)._shellOutputListener) {
            (this as any)._shellOutputListener({
              sessionId: options.sessionId,
              output: mockOutput
            })
          }
        }, 300)
        return { success: true }
      },
      async addListener(eventName: string, listenerFunc: any) {
        console.log('SSH AddListener (Web Mock):', eventName)
        if (eventName === 'shellOutput') {
          (this as any)._shellOutputListener = listenerFunc
        }
        return {
          remove: () => {
            console.log('SSH RemoveListener (Web Mock):', eventName)
            if (eventName === 'shellOutput') {
              (this as any)._shellOutputListener = null
            }
          }
        }
      }
    }
  }
})

export default defineNuxtPlugin(() => {
  return {
    provide: {
      ssh: SSHClient
    }
  }
})

