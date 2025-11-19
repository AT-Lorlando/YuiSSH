<template>
  <div class="space-y-4">
    <!-- Auth Method Selection -->
    <div>
      <label class="block text-sm font-medium text-gray-300 mb-3">
        Authentication Method
      </label>
      <div class="space-y-2">
        <!-- Secure Key (Recommended) -->
        <button
          :class="[
            'w-full p-4 rounded-xl text-left transition-all',
            modelValue === 'secureKey' ? 'neomorph' : 'neomorph-pressed'
          ]"
          @click="$emit('update:modelValue', 'secureKey')"
        >
          <div class="flex items-center gap-3">
            <Icon name="lucide:shield-check" class="w-6 h-6 text-green-400" />
            <div class="flex-1">
              <div class="font-medium text-gray-200">Secure Key 🔐</div>
              <div class="text-xs text-gray-400">Hardware-protected biometric key (Recommended)</div>
            </div>
            <span v-if="keyCount > 0" class="text-xs text-green-400">
              {{ keyCount }} key(s)
            </span>
          </div>
        </button>

        <button
          :class="[
            'w-full p-4 rounded-xl text-left transition-all',
            modelValue === 'password' ? 'neomorph' : 'neomorph-pressed'
          ]"
          @click="$emit('update:modelValue', 'password')"
        >
          <div class="flex items-center gap-3">
            <Icon name="lucide:key" class="w-6 h-6 text-yellow-400" />
            <div>
              <div class="font-medium text-gray-200">Password</div>
              <div class="text-xs text-gray-400">Use password authentication</div>
            </div>
          </div>
        </button>

        <button
          :class="[
            'w-full p-4 rounded-xl text-left transition-all',
            modelValue === 'agent' ? 'neomorph' : 'neomorph-pressed'
          ]"
          @click="$emit('update:modelValue', 'agent')"
        >
          <div class="flex items-center gap-3">
            <Icon name="lucide:shield" class="w-6 h-6 text-purple-400" />
            <div>
              <div class="font-medium text-gray-200">SSH Agent</div>
              <div class="text-xs text-gray-400">Use SSH agent forwarding</div>
            </div>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: 'password' | 'privateKey' | 'secureKey' | 'agent'
  keyCount: number
}>()

defineEmits<{
  'update:modelValue': [value: 'password' | 'privateKey' | 'secureKey' | 'agent']
}>()
</script>
