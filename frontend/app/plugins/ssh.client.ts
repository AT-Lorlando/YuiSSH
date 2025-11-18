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

