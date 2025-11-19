<template>
  <div class="space-y-4">
    <!-- No keys warning -->
    <div v-if="keys.length === 0" class="p-4 rounded-xl bg-yellow-500/10 border border-yellow-500/20">
      <div class="flex items-start gap-3">
        <Icon name="lucide:alert-triangle" class="w-5 h-5 text-yellow-400 flex-shrink-0 mt-0.5" />
        <div class="text-sm">
          <p class="font-medium text-yellow-300 mb-1">No Secure Keys</p>
          <p class="text-yellow-400/80 mb-2">You haven't generated any secure keys yet.</p>
          <button
            class="neomorph-btn px-4 py-2 rounded-lg text-xs font-medium text-gray-200"
            @click="$router.push('/ssh/keys')"
          >
            Generate Secure Key
          </button>
        </div>
      </div>
    </div>

    <!-- Key selection -->
    <div v-else>
      <label class="block text-sm font-medium text-gray-300 mb-2">
        Select Secure Key *
      </label>
      <div class="space-y-2">
        <button
          v-for="key in keys"
          :key="key.keyId"
          :class="[
            'w-full p-4 rounded-xl text-left transition-all',
            selectedKeyId === key.keyId ? 'neomorph' : 'neomorph-pressed'
          ]"
          @click="selectKey(key)"
        >
          <div class="flex items-center gap-3">
            <Icon name="lucide:key" class="w-5 h-5 text-green-400 flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <div class="font-medium text-gray-200">{{ key.label }}</div>
              <div class="text-xs text-gray-400 truncate">{{ fingerprint(key.publicKey) }}</div>
            </div>
            <Icon v-if="selectedKeyId === key.keyId" name="lucide:check-circle" class="w-5 h-5 text-green-400 flex-shrink-0" />
          </div>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  keys: Array<{ keyId: string; label: string; publicKey: string; keyType: string; createdAt: string }>
  selectedKeyId?: string
}>()

const emit = defineEmits<{
  'update:selectedKeyId': [keyId: string]
  'update:selectedKeyLabel': [label: string]
}>()

const selectKey = (key: any) => {
  emit('update:selectedKeyId', key.keyId)
  emit('update:selectedKeyLabel', key.label)
}

const fingerprint = (publicKey: string): string => {
  if (!publicKey) return ''
  const parts = publicKey.split(' ')
  const keyPart = parts[1] || publicKey
  if (keyPart.length > 20) {
    return `${keyPart.substring(0, 8)}...${keyPart.substring(keyPart.length - 8)}`
  }
  return keyPart
}
</script>
