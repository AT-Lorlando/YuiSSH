import type { SSHConnectionOptions, SSHConnectionResult, SSHCommandOptions, SSHCommandResult } from '~/plugins/ssh.client'

export const useSSH = () => {
  const { $ssh } = useNuxtApp()
  const currentSessionId = ref<string | null>(null)
  const isConnected = ref(false)
  const connectionError = ref<string | null>(null)

  const connect = async (options: SSHConnectionOptions): Promise<boolean> => {
    connectionError.value = null

    try {
      const result: SSHConnectionResult = await $ssh.connect(options)

      if (result.success && result.sessionId) {
        currentSessionId.value = result.sessionId
        isConnected.value = true
        return true
      } else {
        connectionError.value = result.message || 'Connection failed'
        return false
      }
    } catch (error: any) {
      connectionError.value = error.message || 'Connection error'
      return false
    }
  }

  const disconnect = async (): Promise<boolean> => {
    if (!currentSessionId.value) return false

    try {
      const result = await $ssh.disconnect({ sessionId: currentSessionId.value })

      if (result.success) {
        currentSessionId.value = null
        isConnected.value = false
        return true
      }
      return false
    } catch (error) {
      console.error('Disconnect error:', error)
      return false
    }
  }

  const executeCommand = async (command: string): Promise<string> => {
    if (!currentSessionId.value) {
      throw new Error('Not connected')
    }

    try {
      const result: SSHCommandResult = await $ssh.executeCommand({
        sessionId: currentSessionId.value,
        command
      })

      if (result.success && result.output) {
        return result.output
      } else {
        throw new Error(result.error || 'Command execution failed')
      }
    } catch (error: any) {
      throw new Error(error.message || 'Command execution error')
    }
  }

  const checkConnection = async (): Promise<boolean> => {
    if (!currentSessionId.value) return false

    try {
      const result = await $ssh.isConnected({ sessionId: currentSessionId.value })
      isConnected.value = result.connected
      return result.connected
    } catch (error) {
      isConnected.value = false
      return false
    }
  }

  // Interactive shell session
  const shellOutputListener = ref<{ remove: () => void } | null>(null)

  const startShellSession = async (callback: (output: string) => void): Promise<boolean> => {
    if (!currentSessionId.value) {
      console.error('Cannot start shell session: not connected')
      return false
    }

    try {
      // Setup listener for shell output
      shellOutputListener.value = await $ssh.addListener('shellOutput', (data) => {
        if (data.sessionId === currentSessionId.value) {
          callback(data.output)
        }
      })

      // Start shell session
      const result = await $ssh.startShellSession({
        sessionId: currentSessionId.value
      })

      return result.success
    } catch (error: any) {
      console.error('Failed to start shell session:', error)
      connectionError.value = error.message || 'Failed to start shell session'
      return false
    }
  }

  const sendToShell = async (command: string): Promise<void> => {
    if (!currentSessionId.value) {
      throw new Error('Not connected')
    }

    try {
      await $ssh.sendToShell({
        sessionId: currentSessionId.value,
        command
      })
    } catch (error: any) {
      throw new Error(error.message || 'Failed to send command')
    }
  }

  const cleanupShellSession = () => {
    if (shellOutputListener.value) {
      shellOutputListener.value.remove()
      shellOutputListener.value = null
    }
  }

  return {
    currentSessionId,
    isConnected,
    connectionError,
    connect,
    disconnect,
    executeCommand,
    checkConnection,
    startShellSession,
    sendToShell,
    cleanupShellSession
  }
}

