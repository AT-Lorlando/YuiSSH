<script setup lang="ts">
const { isAuthenticated } = useAuth()
const router = useRouter()
</script>

<template>
  <div class="neomorph mx-4 mt-4 mb-2 px-6 py-4 flex items-center justify-between">
    <button @click="router.push('/')" class="text-gray-100 font-semibold text-lg">
      MobileApp
    </button>
    
    <button 
      v-if="isAuthenticated" 
      @click="router.push('/profile')" 
      class="neomorph-pressed w-10 h-10 rounded-full flex items-center justify-center"
    >
      <Icon name="lucide:user" class="w-5 h-5 text-blue-400" />
    </button>
    <button 
      v-else 
      @click="router.push('/login')" 
      class="neomorph-btn px-4 py-2 rounded-lg text-sm font-medium text-gray-200"
    >
      Sign In
    </button>
  </div>
</template>
