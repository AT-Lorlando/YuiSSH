<template>
  <div>
    <label class="block text-sm font-medium text-slate-300 mb-2">
      Password
    </label>
    <input
      :value="modelValue"
      type="password"
      placeholder="Enter password"
      class="input-field"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <p class="text-xs text-yellow-400 mt-2 flex items-center gap-1">
      <Icon name="lucide:alert-triangle" class="w-3 h-3" />
      Passwords are stored locally
    </p>
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
