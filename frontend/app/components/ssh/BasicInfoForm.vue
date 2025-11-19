<script setup lang="ts">
import { computed } from 'vue'
import type { SSHHost } from '~/stores/ssh'

const props = defineProps<{
  formData: Partial<SSHHost>
}>()

const emit = defineEmits<{
  'update:formData': [value: Partial<SSHHost>]
}>()

const updateField = (field: keyof SSHHost, value: any) => {
  emit('update:formData', { ...props.formData, [field]: value })
}
</script>

<template>
  <div class="space-y-4">
    <!-- Name -->
    <div>
      <label class="block text-sm font-medium text-gray-300 mb-2">
        Host Name *
      </label>
      <div class="neomorph-pressed rounded-xl overflow-hidden">
        <input
          :value="formData.name"
          type="text"
          placeholder="My Server"
          class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
          @input="updateField('name', ($event.target as HTMLInputElement).value)"
        />
      </div>
    </div>

    <!-- Hostname -->
    <div>
      <label class="block text-sm font-medium text-gray-300 mb-2">
        Hostname / IP Address *
      </label>
      <div class="neomorph-pressed rounded-xl overflow-hidden">
        <input
          :value="formData.hostname"
          type="text"
          placeholder="example.com or 192.168.1.100"
          class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
          @input="updateField('hostname', ($event.target as HTMLInputElement).value)"
        />
      </div>
    </div>

    <!-- Port & Username -->
    <div class="grid grid-cols-2 gap-4">
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-2">
          Port *
        </label>
        <div class="neomorph-pressed rounded-xl overflow-hidden">
          <input
            :value="formData.port"
            type="number"
            placeholder="22"
            class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
            @input="updateField('port', parseInt(($event.target as HTMLInputElement).value))"
          />
        </div>
      </div>
      
      <div>
        <label class="block text-sm font-medium text-gray-300 mb-2">
          Username *
        </label>
        <div class="neomorph-pressed rounded-xl overflow-hidden">
          <input
            :value="formData.username"
            type="text"
            placeholder="root"
            class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none"
            @input="updateField('username', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>
    </div>

    <!-- Description -->
    <div>
      <label class="block text-sm font-medium text-gray-300 mb-2">
        Description
      </label>
      <div class="neomorph-pressed rounded-xl overflow-hidden">
        <textarea
          :value="formData.description"
          placeholder="Optional description..."
          rows="3"
          class="w-full px-4 py-3 bg-transparent text-gray-200 outline-none resize-none"
          @input="updateField('description', ($event.target as HTMLInputElement).value)"
        />
      </div>
    </div>
  </div>
</template>
