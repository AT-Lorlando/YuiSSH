import { defineStore } from 'pinia'

export interface SSHHost {
  id: string
  name: string
  hostname: string
  port: number
  username: string
  authMethod: 'password' | 'privateKey' | 'agent'
  password?: string
  privateKey?: string
  privateKeyPath?: string
  passphrase?: string
  description?: string
  tags?: string[]
  forwardAgent?: boolean
  compression?: boolean
  keepAlive?: boolean
  strictHostKeyChecking?: boolean
  proxyJump?: string
  localForwards?: LocalForward[]
  remoteForwards?: RemoteForward[]
  lastUsed?: string
  createdAt: string
  updatedAt: string
}

export interface LocalForward {
  localPort: number
  remoteHost: string
  remotePort: number
}

export interface RemoteForward {
  remotePort: number
  localHost: string
  localPort: number
}

export const useSSHStore = defineStore('ssh', {
  state: () => ({
    hosts: [] as SSHHost[],
    currentConnection: null as string | null,
  }),

  getters: {
    getHostById: (state) => {
      return (id: string) => state.hosts.find(h => h.id === id)
    },
    
    sortedHosts: (state) => {
      return [...state.hosts].sort((a, b) => {
        if (a.lastUsed && b.lastUsed) {
          return new Date(b.lastUsed).getTime() - new Date(a.lastUsed).getTime()
        }
        if (a.lastUsed) return -1
        if (b.lastUsed) return 1
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      })
    },

    recentHosts: (state) => {
      return state.hosts
        .filter(h => h.lastUsed)
        .sort((a, b) => new Date(b.lastUsed!).getTime() - new Date(a.lastUsed!).getTime())
        .slice(0, 5)
    },

    hostsByTag: (state) => {
      return (tag: string) => state.hosts.filter(h => h.tags?.includes(tag))
    },
  },

  actions: {
    loadHosts() {
      if (process.client) {
        const stored = localStorage.getItem('ssh-hosts')
        if (stored) {
          this.hosts = JSON.parse(stored)
        }
      }
    },

    saveHosts() {
      if (process.client) {
        localStorage.setItem('ssh-hosts', JSON.stringify(this.hosts))
      }
    },

    addHost(host: Omit<SSHHost, 'id' | 'createdAt' | 'updatedAt'>) {
      const newHost: SSHHost = {
        ...host,
        id: crypto.randomUUID(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      }
      this.hosts.push(newHost)
      this.saveHosts()
      return newHost.id
    },

    updateHost(id: string, updates: Partial<SSHHost>) {
      const index = this.hosts.findIndex(h => h.id === id)
      if (index !== -1) {
        const currentHost = this.hosts[index]
        if (currentHost) {
          this.hosts[index] = {
            ...currentHost,
            ...updates,
            id: currentHost.id,
            name: updates.name ?? currentHost.name,
            hostname: updates.hostname ?? currentHost.hostname,
            port: updates.port ?? currentHost.port,
            username: updates.username ?? currentHost.username,
            authMethod: updates.authMethod ?? currentHost.authMethod,
            createdAt: currentHost.createdAt,
            updatedAt: new Date().toISOString(),
          }
          this.saveHosts()
        }
      }
    },

    deleteHost(id: string) {
      const index = this.hosts.findIndex(h => h.id === id)
      if (index !== -1) {
        this.hosts.splice(index, 1)
        this.saveHosts()
      }
    },

    markHostAsUsed(id: string) {
      this.updateHost(id, { lastUsed: new Date().toISOString() })
    },

    duplicateHost(id: string) {
      const host = this.getHostById(id)
      if (host) {
        const { id: _, createdAt, updatedAt, lastUsed, ...hostData } = host
        return this.addHost({
          ...hostData,
          name: `${host.name} (Copy)`,
        })
      }
    },

    exportHost(id: string): string {
      const host = this.getHostById(id)
      if (!host) return ''
      
      let config = `Host ${host.name}\n`
      config += `  HostName ${host.hostname}\n`
      config += `  Port ${host.port}\n`
      config += `  User ${host.username}\n`
      
      if (host.privateKeyPath) {
        config += `  IdentityFile ${host.privateKeyPath}\n`
      }
      
      if (host.forwardAgent) {
        config += `  ForwardAgent yes\n`
      }
      
      if (host.compression) {
        config += `  Compression yes\n`
      }
      
      if (host.strictHostKeyChecking !== undefined) {
        config += `  StrictHostKeyChecking ${host.strictHostKeyChecking ? 'yes' : 'no'}\n`
      }
      
      if (host.proxyJump) {
        config += `  ProxyJump ${host.proxyJump}\n`
      }
      
      host.localForwards?.forEach(fwd => {
        config += `  LocalForward ${fwd.localPort} ${fwd.remoteHost}:${fwd.remotePort}\n`
      })
      
      host.remoteForwards?.forEach(fwd => {
        config += `  RemoteForward ${fwd.remotePort} ${fwd.localHost}:${fwd.localPort}\n`
      })
      
      return config
    },

    exportAllHosts(): string {
      return this.hosts.map(h => this.exportHost(h.id)).join('\n\n')
    },

    importFromConfig(config: string) {
      // Simple parser for SSH config format
      const hosts: Omit<SSHHost, 'id' | 'createdAt' | 'updatedAt'>[] = []
      const lines = config.split('\n')
      let currentHost: any = null

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith('#')) continue

        if (trimmed.startsWith('Host ')) {
          if (currentHost) {
            hosts.push(currentHost)
          }
          currentHost = {
            name: trimmed.substring(5).trim(),
            hostname: '',
            port: 22,
            username: '',
            authMethod: 'privateKey' as const,
          }
        } else if (currentHost) {
          const parts = trimmed.split(/\s+/)
          const key = parts[0]
          const value = parts.slice(1).join(' ')
          
          if (!key) continue
          
          switch (key.toLowerCase()) {
            case 'hostname':
              currentHost.hostname = value
              break
            case 'port':
              currentHost.port = parseInt(value) || 22
              break
            case 'user':
              currentHost.username = value
              break
            case 'identityfile':
              currentHost.privateKeyPath = value
              break
            case 'forwardagent':
              currentHost.forwardAgent = value.toLowerCase() === 'yes'
              break
            case 'compression':
              currentHost.compression = value.toLowerCase() === 'yes'
              break
            case 'stricthostkeychecking':
              currentHost.strictHostKeyChecking = value.toLowerCase() === 'yes'
              break
            case 'proxyjump':
              currentHost.proxyJump = value
              break
          }
        }
      }

      if (currentHost) {
        hosts.push(currentHost)
      }

      hosts.forEach(host => this.addHost(host))
      return hosts.length
    },
  },
})

