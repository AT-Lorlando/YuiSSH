<script setup lang="ts">
import { Page } from 'konsta/vue'
import { useSSHStore } from '~/stores/ssh'

const router = useRouter()
const sshStore = useSSHStore()

onMounted(() => {
  sshStore.loadHosts()
})

const recentHosts = computed(() => sshStore.recentHosts.slice(0, 3))
const totalHosts = computed(() => sshStore.hosts.length)

const navigateToHost = (hostId: string) => {
  router.push(`/ssh/connect/${hostId}`)
}
</script>

<template>
  <Page>
    <Breadcrumbs />
    
    <div class="min-h-screen px-6 py-8">
      <!-- Hero Section -->
      <div class="text-center mb-8">
        <div class="w-20 h-20 mx-auto mb-6 neomorph flex items-center justify-center">
          <Icon name="lucide:terminal" class="w-10 h-10 text-blue-400" />
        </div>
        <h1 class="text-4xl font-bold mb-3 text-gray-100">
          YuiSSH
        </h1>
        <p class="text-gray-400 text-lg">
          Manage Your SSH Connections
        </p>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-2 gap-4 mb-8 max-w-md mx-auto">
        <div class="neomorph p-5 rounded-xl text-center">
          <div class="text-3xl font-bold text-blue-400 mb-1">{{ totalHosts }}</div>
          <div class="text-sm text-gray-400">Total Hosts</div>
        </div>
        <div class="neomorph p-5 rounded-xl text-center">
          <div class="text-3xl font-bold text-green-400 mb-1">{{ recentHosts.length }}</div>
          <div class="text-sm text-gray-400">Recent</div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="max-w-md mx-auto space-y-4 mb-8">
        <button
          class="neomorph-btn w-full p-6 rounded-2xl flex items-center gap-4 hover:scale-105 transition-transform"
          @click="router.push('/ssh')"
        >
          <div class="neomorph-pressed p-4 rounded-xl">
            <Icon name="lucide:list" class="w-8 h-8 text-blue-400" />
          </div>
          <div class="flex-1 text-left">
            <h3 class="text-lg font-semibold text-gray-100 mb-1">All Connections</h3>
            <p class="text-sm text-gray-400">View and manage all SSH hosts</p>
          </div>
          <Icon name="lucide:chevron-right" class="w-6 h-6 text-gray-400" />
        </button>

        <button
          class="neomorph-btn w-full p-6 rounded-2xl flex items-center gap-4 hover:scale-105 transition-transform"
          @click="router.push('/ssh/new')"
        >
          <div class="neomorph-pressed p-4 rounded-xl">
            <Icon name="lucide:plus-circle" class="w-8 h-8 text-green-400" />
          </div>
          <div class="flex-1 text-left">
            <h3 class="text-lg font-semibold text-gray-100 mb-1">New Connection</h3>
            <p class="text-sm text-gray-400">Add a new SSH host</p>
          </div>
          <Icon name="lucide:chevron-right" class="w-6 h-6 text-gray-400" />
        </button>
      </div>

      <!-- Recent Connections -->
      <div v-if="recentHosts.length > 0" class="max-w-md mx-auto">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-xl font-semibold text-gray-200">Recent Connections</h2>
          <button
            class="text-sm text-blue-400 hover:text-blue-300"
            @click="router.push('/ssh')"
          >
            View All
          </button>
        </div>

        <div class="space-y-3">
          <button
            v-for="host in recentHosts"
            :key="host.id"
            class="neomorph w-full p-4 rounded-xl text-left hover:scale-105 transition-transform"
            @click="navigateToHost(host.id)"
          >
            <div class="flex items-center gap-3">
              <div class="neomorph-pressed p-3 rounded-lg">
                <Icon name="lucide:server" class="w-6 h-6 text-blue-400" />
              </div>
              <div class="flex-1 min-w-0">
                <h3 class="font-semibold text-gray-100 truncate">{{ host.name }}</h3>
                <p class="text-sm text-gray-400 truncate">{{ host.username }}@{{ host.hostname }}</p>
              </div>
              <Icon name="lucide:chevron-right" class="w-5 h-5 text-gray-400 flex-shrink-0" />
            </div>
          </button>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="max-w-md mx-auto text-center py-8">
        <div class="neomorph-pressed w-24 h-24 rounded-full mx-auto mb-6 flex items-center justify-center">
          <Icon name="lucide:inbox" class="w-12 h-12 text-gray-500" />
        </div>
        <h3 class="text-2xl font-bold text-gray-300 mb-2">No Connections Yet</h3>
        <p class="text-gray-500 mb-6">Get started by adding your first SSH host</p>
        <button
          class="neomorph-btn px-8 py-3 rounded-xl font-medium text-gray-200 inline-flex items-center gap-2"
          @click="router.push('/ssh/new')"
        >
          <Icon name="lucide:plus" class="w-5 h-5" />
          Add First Host
        </button>
      </div>
    </div>
  </Page>
</template>
