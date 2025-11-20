<template>
  <div class="space-y-3">
    <!-- Secure Key (Recommended) -->
    <button
      :class="[
        'w-full p-4 rounded-xl text-left transition-all border',
        modelValue === 'secureKey' 
          ? 'border-blue-500 bg-blue-500/10' 
          : 'border-slate-700 bg-slate-800/50 hover:bg-slate-800'
      ]"
      @click="$emit('update:modelValue', 'secureKey')"
    >
      <div class="flex items-center gap-3">
        <Icon name="lucide:shield-check" class="w-6 h-6 text-green-400" />
        <div class="flex-1">
          <div class="font-medium text-slate-200">Secure Key 🔐</div>
          <div class="text-xs text-slate-400">Hardware-protected biometric key (Recommended)</div>
        </div>
        <span v-if="keyCount > 0" class="text-xs text-green-400">
          {{ keyCount }} key(s)
        </span>
      </div>
    </button>

    <!-- Password -->
    <button
      :class="[
        'w-full p-4 rounded-xl text-left transition-all border',
        modelValue === 'password' 
          ? 'border-blue-500 bg-blue-500/10' 
          : 'border-slate-700 bg-slate-800/50 hover:bg-slate-800'
      ]"
      @click="$emit('update:modelValue', 'password')"
    >
      <div class="flex items-center gap-3">
        <Icon name="lucide:key" class="w-6 h-6 text-yellow-400" />
        <div>
          <div class="font-medium text-slate-200">Password</div>
          <div class="text-xs text-slate-400">Use password authentication</div>
        </div>
      </div>
    </button>

    <!-- SSH Agent -->
    <button
      :class="[
        'w-full p-4 rounded-xl text-left transition-all border',
        modelValue === 'agent' 
          ? 'border-blue-500 bg-blue-500/10' 
          : 'border-slate-700 bg-slate-800/50 hover:bg-slate-800'
      ]"
      @click="$emit('update:modelValue', 'agent')"
    >
      <div class="flex items-center gap-3">
        <Icon name="lucide:shield" class="w-6 h-6 text-purple-400" />
        <div>
          <div class="font-medium text-slate-200">SSH Agent</div>
          <div class="text-xs text-slate-400">Use SSH agent forwarding</div>
        </div>
      </div>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue?: 'password' | 'privateKey' | 'secureKey' | 'agent'
  keyCount: number
}>()

defineEmits<{
  'update:modelValue': [value: 'password' | 'privateKey' | 'secureKey' | 'agent']
}>()
</script>
