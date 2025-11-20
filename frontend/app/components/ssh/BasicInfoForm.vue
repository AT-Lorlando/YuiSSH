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
      <label class="block text-sm font-medium text-slate-300 mb-2">
        Host Name <span class="text-red-400">*</span>
      </label>
      <input
        :value="formData.name"
        type="text"
        placeholder="My Server"
        class="input-field"
        @input="updateField('name', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <!-- Hostname -->
    <div>
      <label class="block text-sm font-medium text-slate-300 mb-2">
        Hostname / IP Address <span class="text-red-400">*</span>
      </label>
      <input
        :value="formData.hostname"
        type="text"
        placeholder="example.com or 192.168.1.100"
        class="input-field"
        @input="updateField('hostname', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <!-- Port -->
    <div>
      <label class="block text-sm font-medium text-slate-300 mb-2">
        Port <span class="text-red-400">*</span>
      </label>
      <input
        :value="formData.port"
        type="number"
        placeholder="22"
        class="input-field"
        @input="updateField('port', parseInt(($event.target as HTMLInputElement).value))"
      />
    </div>

    <!-- Username -->
    <div>
      <label class="block text-sm font-medium text-slate-300 mb-2">
        Username <span class="text-red-400">*</span>
      </label>
      <input
        :value="formData.username"
        type="text"
        placeholder="root"
        class="input-field"
        @input="updateField('username', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <!-- Description -->
    <div>
      <label class="block text-sm font-medium text-slate-300 mb-2">
        Description
      </label>
      <textarea
        :value="formData.description"
        placeholder="Optional description..."
        rows="3"
        class="input-field resize-none"
        @input="updateField('description', ($event.target as HTMLInputElement).value)"
      />
    </div>
  </div>
</template>
