<script setup lang="ts">
import type { SSHHost } from '~/stores/ssh'

const props = defineProps<{
  host: SSHHost
}>()

const emit = defineEmits<{
  connect: []
  edit: []
  delete: []
  duplicate: []
}>()

const formatDate = (date: string | undefined) => {
  if (!date) return 'Never'
  const d = new Date(date)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  if (hours < 24) return `${hours}h ago`
  if (days < 7) return `${days}d ago`
  
  return d.toLocaleDateString()
}

const getAuthIcon = (method: string) => {
  switch (method) {
    case 'password': return 'lucide:key'
    case 'privateKey': return 'lucide:file-key'
    case 'agent': return 'lucide:shield'
    default: return 'lucide:lock'
  }
}

const showActions = ref(false)
</script>

<template>
  <div class="neomorph p-5 mb-4 transition-all hover:scale-[1.02]">
    <!-- Header -->
    <div class="flex items-start justify-between mb-4">
      <div class="flex-1">
        <div class="flex items-center gap-2 mb-1">
          <Icon name="lucide:server" class="w-5 h-5 text-blue-400" />
          <h3 class="text-lg font-semibold text-gray-100">{{ host.name }}</h3>
        </div>
        <p class="text-sm text-gray-400">{{ host.username }}@{{ host.hostname }}</p>
      </div>
      
      <!-- Actions Menu -->
      <button 
        class="neomorph-btn p-2 rounded-lg"
        @click="showActions = !showActions"
      >
        <Icon name="lucide:more-vertical" class="w-5 h-5 text-gray-300" />
      </button>
    </div>

    <!-- Actions Dropdown -->
    <div 
      v-if="showActions" 
      class="neomorph-pressed p-3 rounded-xl mb-4 space-y-2"
    >
      <button
        class="w-full text-left px-3 py-2 text-sm text-gray-300 hover:text-blue-400 transition-colors flex items-center gap-2"
        @click="emit('edit'); showActions = false"
      >
        <Icon name="lucide:edit" class="w-4 h-4" />
        Edit
      </button>
      <button
        class="w-full text-left px-3 py-2 text-sm text-gray-300 hover:text-blue-400 transition-colors flex items-center gap-2"
        @click="emit('duplicate'); showActions = false"
      >
        <Icon name="lucide:copy" class="w-4 h-4" />
        Duplicate
      </button>
      <button
        class="w-full text-left px-3 py-2 text-sm text-red-400 hover:text-red-300 transition-colors flex items-center gap-2"
        @click="emit('delete'); showActions = false"
      >
        <Icon name="lucide:trash-2" class="w-4 h-4" />
        Delete
      </button>
    </div>

    <!-- Info Grid -->
    <div class="grid grid-cols-2 gap-3 mb-4">
      <div class="neomorph-pressed px-3 py-2 rounded-lg">
        <div class="flex items-center gap-2 text-gray-400 text-xs mb-1">
          <Icon name="lucide:network" class="w-3 h-3" />
          <span>Port</span>
        </div>
        <p class="text-sm font-medium text-gray-200">{{ host.port }}</p>
      </div>
      
      <div class="neomorph-pressed px-3 py-2 rounded-lg">
        <div class="flex items-center gap-2 text-gray-400 text-xs mb-1">
          <Icon :name="getAuthIcon(host.authMethod)" class="w-3 h-3" />
          <span>Auth</span>
        </div>
        <p class="text-sm font-medium text-gray-200 capitalize">{{ host.authMethod }}</p>
      </div>
    </div>

    <!-- Tags -->
    <div v-if="host.tags && host.tags.length > 0" class="flex flex-wrap gap-2 mb-4">
      <span 
        v-for="tag in host.tags" 
        :key="tag"
        class="px-2 py-1 text-xs rounded-lg neomorph-pressed text-gray-300"
      >
        #{{ tag }}
      </span>
    </div>

    <!-- Description -->
    <p v-if="host.description" class="text-sm text-gray-400 mb-4 italic">
      {{ host.description }}
    </p>

    <!-- Footer -->
    <div class="flex items-center justify-between pt-3 border-t border-gray-700">
      <div class="text-xs text-gray-500">
        <Icon name="lucide:clock" class="w-3 h-3 inline mr-1" />
        {{ formatDate(host.lastUsed) }}
      </div>
      
      <button
        class="neomorph-btn px-6 py-2 rounded-lg font-medium text-gray-200 flex items-center gap-2 hover:scale-105 transition-transform"
        @click="emit('connect')"
      >
        <Icon name="lucide:plug" class="w-4 h-4" />
        Connect
      </button>
    </div>
  </div>
</template>

