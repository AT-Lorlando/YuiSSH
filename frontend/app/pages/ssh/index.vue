<script setup lang="ts">
import { useSSHStore } from '~/stores/ssh'

const sshStore = useSSHStore()
const router = useRouter()

const searchQuery = ref('')
const showDeleteConfirm = ref(false)
const hostToDelete = ref<string | null>(null)

// Load hosts on mount
onMounted(() => {
  sshStore.loadHosts()
})

// Filtered hosts based on search
const filteredHosts = computed(() => {
  if (!searchQuery.value) {
    return sshStore.sortedHosts
  }
  
  const query = searchQuery.value.toLowerCase()
  return sshStore.sortedHosts.filter(host =>
    host.name.toLowerCase().includes(query) ||
    host.hostname.toLowerCase().includes(query) ||
    host.username.toLowerCase().includes(query) ||
    host.tags?.some(tag => tag.toLowerCase().includes(query))
  )
})

const recentHosts = computed(() => sshStore.recentHosts)

const handleConnect = (hostId: string) => {
  router.push(`/ssh/connect/${hostId}`)
}

const handleEdit = (hostId: string) => {
  router.push(`/ssh/edit/${hostId}`)
}

const handleDelete = (hostId: string) => {
  hostToDelete.value = hostId
  showDeleteConfirm.value = true
}

const confirmDelete = () => {
  if (hostToDelete.value) {
    sshStore.deleteHost(hostToDelete.value)
    hostToDelete.value = null
  }
  showDeleteConfirm.value = false
}

const handleDuplicate = (hostId: string) => {
  sshStore.duplicateHost(hostId)
}

const createNewHost = () => {
  router.push('/ssh/new')
}
</script>

<template>
    <div class="">
      <!-- Header -->
      <div class="mb-6">
        <!-- Search -->
        <div class="neomorph-pressed rounded-xl overflow-hidden">
          <div class="flex items-center px-4 py-3">
            <Icon name="lucide:search" class="w-5 h-5 text-gray-400 mr-3" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search hosts..."
              class="flex-1 bg-transparent text-gray-200 outline-none placeholder-gray-500"
            />
            <button
              v-if="searchQuery"
              @click="searchQuery = ''"
              class="ml-2"
            >
              <Icon name="lucide:x" class="w-5 h-5 text-gray-400" />
            </button>
          </div>
        </div>
      </div>

      <!-- Recent Hosts -->
      <div v-if="recentHosts.length > 0 && !searchQuery" class="mb-6">
        <h2 class="text-xl font-semibold text-gray-200 mb-3 flex items-center gap-2">
          <Icon name="lucide:history" class="w-5 h-5 text-blue-400" />
          Recent Connections
        </h2>
        <div class="space-y-3">
          <SSHHostCard
            v-for="host in recentHosts"
            :key="host.id"
            :host="host"
            @connect="handleConnect(host.id)"
            @edit="handleEdit(host.id)"
            @delete="handleDelete(host.id)"
            @duplicate="handleDuplicate(host.id)"
          />
        </div>
      </div>

      <!-- All Hosts -->
      <div v-if="filteredHosts.length > 0">
        <h2 class="text-xl font-semibold text-gray-200 mb-3 flex items-center gap-2">
          <Icon name="lucide:list" class="w-5 h-5 text-blue-400" />
          {{ searchQuery ? 'Search Results' : 'All Hosts' }}
        </h2>
        <div class="space-y-3">
          <SSHHostCard
            v-for="host in filteredHosts"
            :key="host.id"
            :host="host"
            @connect="handleConnect(host.id)"
            @edit="handleEdit(host.id)"
            @delete="handleDelete(host.id)"
            @duplicate="handleDuplicate(host.id)"
          />
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="sshStore.hosts.length === 0" class="text-center py-16">
        <div class="neomorph-pressed w-24 h-24 rounded-full mx-auto mb-6 flex items-center justify-center">
          <Icon name="lucide:server-off" class="w-12 h-12 text-gray-500" />
        </div>
        <h3 class="text-2xl font-bold text-gray-300 mb-2">No SSH Hosts</h3>
        <p class="text-gray-500 mb-6">Get started by adding your first host</p>
        <button
          class="neomorph-btn px-8 py-3 rounded-xl font-medium text-gray-200 inline-flex items-center gap-2"
          @click="createNewHost"
        >
          <Icon name="lucide:plus" class="w-5 h-5" />
          Add First Host
        </button>
      </div>

      <!-- No Search Results -->
      <div v-if="sshStore.hosts.length > 0 && filteredHosts.length === 0" class="text-center py-16">
        <div class="neomorph-pressed w-24 h-24 rounded-full mx-auto mb-6 flex items-center justify-center">
          <Icon name="lucide:search-x" class="w-12 h-12 text-gray-500" />
        </div>
        <h3 class="text-2xl font-bold text-gray-300 mb-2">No Results</h3>
        <p class="text-gray-500 mb-6">No hosts match "{{ searchQuery }}"</p>
        <button
          class="neomorph-btn px-6 py-3 rounded-xl font-medium text-gray-200"
          @click="searchQuery = ''"
        >
          Clear Search
        </button>
      </div>

      <!-- Quick Actions FAB -->
      <div class="fixed bottom-6 right-6 flex flex-col gap-3">
        <button
          class="neomorph-btn w-12 h-12 rounded-full flex items-center justify-center shadow-2xl bg-teal-500"
          @click="createNewHost"
          title="Add new host"
        >
          <Icon name="lucide:plus" class="w-6 h-6 text-gray-200" />
        </button>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <Teleport to="body">
      <div
        v-if="showDeleteConfirm"
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        style="background: rgba(0, 0, 0, 0.7)"
        @click.self="showDeleteConfirm = false"
      >
        <div class="neomorph p-6 rounded-2xl max-w-sm w-full">
          <div class="text-center mb-6">
            <div class="neomorph-pressed w-16 h-16 rounded-full mx-auto mb-4 flex items-center justify-center">
              <Icon name="lucide:alert-triangle" class="w-8 h-8 text-red-400" />
            </div>
            <h3 class="text-xl font-bold text-gray-100 mb-2">Delete Host?</h3>
            <p class="text-gray-400 text-sm">This action cannot be undone.</p>
          </div>
          
          <div class="flex gap-3">
            <button
              class="flex-1 neomorph-btn py-3 rounded-xl font-medium text-gray-200"
              @click="showDeleteConfirm = false"
            >
              Cancel
            </button>
            <button
              class="flex-1 py-3 rounded-xl font-medium text-white"
              style="background: linear-gradient(145deg, #dc2626, #b91c1c); box-shadow: 6px 6px 12px rgba(0, 0, 0, 0.4), -6px -6px 12px rgba(239, 68, 68, 0.1);"
              @click="confirmDelete"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </Teleport>
</template>

