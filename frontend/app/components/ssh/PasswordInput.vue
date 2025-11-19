<template>
  <div class="space-y-4">
    <div>
      <label class="block text-sm font-medium text-gray-300 mb-2">
        Password
      </label>
      <div class="neomorph-pressed rounded-xl overflow-hidden">
        <input
          :value="modelValue"
          type="password"
          placeholder="Enter password"
          class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
          @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        />
      </div>
      <p class="text-xs text-yellow-400 mt-2 flex items-center gap-1">
        <Icon name="lucide:alert-triangle" class="w-3 h-3" />
        Passwords are stored locally
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  modelValue?: string
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>
