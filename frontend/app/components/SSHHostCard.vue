<script setup lang="ts">
import type { SSHHost } from '~/stores/ssh'
import { onClickOutside } from '@vueuse/core'

const props = defineProps<{
  host: SSHHost
}>()

const emit = defineEmits<{
  connect: []
  edit: []
  delete: []
  duplicate: []
}>()

const showActions = ref(false)
const actionsRef = ref(null)
const buttonRef = ref<HTMLElement | null>(null)
const menuPosition = ref({ top: 0, left: 0 })

// Calculate menu position
const updateMenuPosition = () => {
  if (buttonRef.value) {
    const rect = buttonRef.value.getBoundingClientRect()
    menuPosition.value = {
      top: rect.bottom + 8,
      left: rect.right - 192 // 192px = w-48
    }
  }
}

// Toggle menu and update position
const toggleMenu = () => {
  showActions.value = !showActions.value
  if (showActions.value) {
    nextTick(() => updateMenuPosition())
  }
}

// Close actions when clicking outside
onClickOutside(actionsRef, () => {
  showActions.value = false
})
</script>

<template>
  <div class="card flex items-center justify-between p-4 mb-3 transition-all active:scale-[0.99] relative group">
    <!-- Main Clickable Area -->
    <div class="flex items-center gap-4 flex-1 min-w-0 cursor-pointer" @click="emit('connect')">
      <div class="w-12 h-12 flex items-center justify-center rounded-full bg-blue-500/10 text-blue-400 group-hover:bg-blue-500/20 transition-colors flex-shrink-0">
        <Icon name="lucide:terminal" class="w-6 h-6" />
      </div>
      <div class="truncate">
        <h3 class="text-base font-semibold text-slate-200 truncate">{{ host.name }}</h3>
        <p class="text-sm text-slate-500 truncate">{{ host.username }}@{{ host.hostname }}</p>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center gap-1 ml-2">
      <div class="relative">
        <button 
          ref="buttonRef"
          @click.stop="toggleMenu"
          class="w-10 h-10 flex items-center justify-center p-2 rounded-full text-slate-500 hover:text-slate-300 hover:bg-slate-700/50 transition-colors bg-slate-800"
        >
          <Icon name="lucide:more-vertical" class="w-5 h-5" />
        </button>
      </div>

      <button 
        @click.stop="emit('connect')"
        class="w-10 h-10 flex items-center justify-center rounded-full bg-blue-600 text-white shadow-lg shadow-blue-900/20 active:scale-95 transition-all ml-1"
      >
        <Icon name="lucide:chevron-right" class="w-5 h-5" />
      </button>
    </div>

    <!-- Teleported Dropdown Menu -->
    <Teleport to="body">
      <div 
        v-show="showActions"
        ref="actionsRef"
        :style="{ top: `${menuPosition.top}px`, left: `${menuPosition.left}px` }"
        class="fixed z-[100] w-48 bg-slate-800/95 backdrop-blur-md border border-slate-700 rounded-xl shadow-xl overflow-hidden"
      >
        <button
          class="w-full text-left px-4 py-3 text-sm text-slate-300 hover:bg-slate-700/50 transition-colors flex items-center gap-2"
          @click="emit('edit'); showActions = false"
        >
          <Icon name="lucide:edit" class="w-4 h-4" />
          Edit
        </button>
        <button
          class="w-full text-left px-4 py-3 text-sm text-slate-300 hover:bg-slate-700/50 transition-colors flex items-center gap-2"
          @click="emit('duplicate'); showActions = false"
        >
          <Icon name="lucide:copy" class="w-4 h-4" />
          Duplicate
        </button>
        <div class="h-px bg-slate-700 my-1"></div>
        <button
          class="w-full text-left px-4 py-3 text-sm text-red-400 hover:bg-red-500/10 transition-colors flex items-center gap-2"
          @click="emit('delete'); showActions = false"
        >
          <Icon name="lucide:trash-2" class="w-4 h-4" />
          Delete
        </button>
      </div>
    </Teleport>
  </div>
</template>

