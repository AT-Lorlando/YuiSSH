<template>
  <div class="w-full h-full min-h-screen bg-black">
      <header class="safe-area-top bg-slate-900/80 backdrop-blur-md border-b border-slate-800 sticky top-0 z-50">
      <div class="px-6 pt-4 flex items-center justify-between">
        <h1 class="text-xl font-bold bg-gradient-to-r from-blue-400 to-blue-600 bg-clip-text text-transparent">
          YuiSSH
        </h1>
        <button @click="router.push('/profile')" class="p-2 rounded-full hover:bg-slate-800 transition-colors">
          <Icon name="lucide:user" class="w-6 h-6 text-slate-400" />
        </button>
      </div>
      <Breadcrumbs />
      
      <!-- Navigation Tabs -->
      <div class="px-6 flex gap-1 pb-2">
        <NuxtLink 
          to="/ssh" 
          class="flex-1 text-center py-2 px-4 rounded-lg font-medium transition-colors"
          :class="isActive('/ssh') ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-slate-300 hover:bg-slate-800/50'"
        >
          Hosts
        </NuxtLink>
        <NuxtLink 
          to="/keys" 
          class="flex-1 text-center py-2 px-4 rounded-lg font-medium transition-colors"
          :class="isActive('/keys') ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-slate-300 hover:bg-slate-800/50'"
        >
          Keys
        </NuxtLink>
      </div>
    </header>
    <slot />
  </div>
</template>

<script setup lang="ts">
const route = useRoute()
const router = useRouter()

const isActive = (path: string) => route.path === path
</script>